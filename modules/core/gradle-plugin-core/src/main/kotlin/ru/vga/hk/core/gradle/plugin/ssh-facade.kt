/*
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.vga.hk.core.gradle.plugin

import com.jcraft.jsch.*
import java.io.*
import java.util.*


interface SessionCallback {
    fun executeCommand(command: String): String?
    fun getContent(path: String): ByteArray
    fun getContent(path: String, localFile: File)
    fun withInputStream(path: String, handler: (stream: InputStream?) -> Unit)
    fun upload(path: String, content: ByteArray)
    fun upload(path: String, local: File)
    fun exists(path: String): Boolean
    fun delete(path: String)
    fun mkDir(path: String, owner: String? = null, permissions: String? = null)

}

class SshFacade(private val sshConfig: SshConfig) {

    fun withSession(execute: SessionCallback.() -> Unit) {
        println("starting session at ${sshConfig.host}")
        val jsch = JSch()
        val session = jsch.getSession(sshConfig.login, sshConfig.host, 22);
        val conf = Properties()
        conf["StrictHostKeyChecking"] = "no"
        session.setConfig(conf)

        session.userInfo = object : UserInfo {
            override fun getPassphrase(): String {
                throw IllegalStateException("no passhrase")
            }

            override fun getPassword(): String {
                return sshConfig.password
            }

            override fun promptPassword(message: String?): Boolean {
                return true
            }

            override fun promptPassphrase(message: String?): Boolean {
                return false
            }

            override fun promptYesNo(message: String?): Boolean {
                return true
            }

            override fun showMessage(message: String?) {
                println(message)
            }

        }
        session.connect()
        try {

            execute(object : SessionCallback {
                override fun executeCommand(command: String): String? {
                    println("executing command \"$command\"")

                    val channelExec = session.openChannel("exec") as ChannelExec

                    channelExec.setCommand(command)

                    channelExec.setErrStream(System.err)

                    val inputStream = channelExec.getInputStream()
                    val output = StringWriter()

                    channelExec.connect(5000)
                    try {

                        val tmp = ByteArray(1024)
                        while (true) {
                            while (inputStream.available() > 0) {
                                val i = inputStream.read(tmp, 0, 1024)
                                if (i < 0) break
                                output.write(String(tmp, 0, i))
                                print(String(tmp, 0, i))
                            }
                            if (channelExec.isClosed()) {
                                if (inputStream.available() > 0) continue
                                if (channelExec.exitStatus != 0) {
                                    println(output)
                                    throw IllegalStateException("exit status is ${channelExec.exitStatus}")
                                }
                                break
                            }
                            try {
                                Thread.sleep(100)
                            } catch (ee: Exception) {
                            }
                        }
                        return output.toString().let { if (it.length == 0) null else it }
                    } finally {
                        channelExec.disconnect()
                    }
                }

                override fun getContent(path: String): ByteArray {
                    println("getting content of ${path}")
                    var result: ByteArray? = null
                    withInputStream(path) {
                        if (it != null) {
                            val baos = ByteArrayOutputStream();
                            it.copyTo(baos, 256)
                            result = baos.toByteArray()
                        }
                    }
                    return result!!;
                }

                override fun getContent(path: String, localFile: File) {
                    println("writing content of ${path} to file ${localFile.absolutePath}")
                    val outputStream = FileOutputStream(localFile)
                    withInputStream(path) {
                        it!!.copyTo(outputStream)
                        outputStream.flush()
                    }
                }

                override fun withInputStream(path: String, handler: (stream: InputStream?) -> Unit) {
                    val channel = session.openChannel("sftp") as ChannelSftp
                    channel.connect()
                    try {
                        try {
                            val inputStream = channel.get(path)
                            try {
                                handler(inputStream)
                            } finally {
                                inputStream.close()
                            }

                        } catch (e: SftpException) {
                            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                                handler(null)
                                return;
                            } else {
                                // something else went wrong
                                throw e
                            }
                        }
                    } finally {
                        channel.disconnect()
                    }
                }

                override fun upload(path: String, content: ByteArray) {
                    println("uploading content to ${path}")
                    val channel = session.openChannel("sftp") as ChannelSftp
                    channel.connect()
                    try {
                        channel.put(ByteArrayInputStream(content), path)
                    } finally {
                        channel.disconnect()
                    }
                }

                private fun uploadInternal(
                    sourceFile: File, destPath: String,
                    sftpChannel: ChannelSftp
                ) {
                    if (sourceFile.isFile) {
                        sftpChannel.cd(destPath)
                        if (!sourceFile.name.startsWith(".")) {
                            sftpChannel.put(
                                sourceFile.absolutePath,
                                sourceFile.name,
                                ChannelSftp.OVERWRITE
                            )
                        }
                        return;
                    }
                    val files = sourceFile.listFiles()
                    if (files != null && files.size > 0 && !sourceFile.name.startsWith(".")) {
                        sftpChannel.cd(destPath)
                        val destFilePath = destPath + "/" + sourceFile.name
                        sftpChannel.mkdir(sourceFile.name)
                        if (!exists(destFilePath)) {
                            mkDir(destFilePath);
                        }
                        for (i in files.indices) {
                            uploadInternal(files[i], destPath + "/" + sourceFile.name, sftpChannel)
                        }
                    }
                }

                override fun upload(path: String, local: File) {
                    println("uploading content of file ${local.absolutePath} to ${path}")
                    val channel = session.openChannel("sftp") as ChannelSftp
                    channel.connect()
                    try {
                        uploadInternal(local, path, channel)
                    } finally {
                        channel.disconnect()
                    }
                }


                override fun exists(path: String): Boolean {
                    println("checking whether file ${path} exists")
                    var res = executeCommand("(test -f \"$path\" && echo 1) || echo 0")!!.trim()
                    if (res == "0") {
                        res = executeCommand("(test -d \"$path\" && echo 1) || echo 0")!!.trim()
                    }
                    return "1" == res;
                }


                override fun delete(path: String) {
                    println("deleting ${path}")
                    executeCommand("sudo rm -rf \"${path}\"")
                }

                override fun mkDir(path: String, owner: String?, permissions: String?) {
                    println("making dir ${path} with owner ${owner} and permissions ${permissions}")
                    executeCommand("sudo mkdir \"${path}\"")
                    val ownerVal = owner ?: sshConfig.login
                    executeCommand("sudo chown -R ${ownerVal}:${ownerVal} \"${path}\"")
                    if (permissions != null) {
                        executeCommand("chmod -R ${permissions} \"${path}\"")
                    }
                }

            });

        } finally {
            session.disconnect();
        }

    }
}