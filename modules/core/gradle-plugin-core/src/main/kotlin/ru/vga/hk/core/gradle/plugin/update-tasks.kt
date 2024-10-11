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

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class BaseUpdateTask() : DefaultTask() {
    @Internal
    protected lateinit var extension: HomeKeeperExtension

    @Internal
    protected lateinit var distDir: File

    @Internal
    protected lateinit var sshFacade: SshFacade


    protected fun getProcessId(callback: SessionCallback): String? {
        val result = callback.executeCommand("""ps aux|grep "home-keeper-tag=true"""")!!
        return result.lines()?.find { it.contains("/bin/java") }?.let {
            it.split(" ").filter { it.isNotEmpty() }[1].trim()
        }
    }

    protected fun stopApp(callback: SessionCallback) {
        println("stopping app")
        var processId: String? = getProcessId(callback)
        if (processId == null) {
            println("app is not running")
            return
        }
        for (i in 0..10) {
            println("stopping process $processId")
            callback.executeCommand("kill $processId")
            Thread.sleep(1000)
            processId = getProcessId(callback)
            if (processId == null) {
                println("process was stopped")
                return
            }
            println("process was not stopped")
        }
        println("killing process $processId")
        callback.executeCommand("kill -9 $processId")
        Thread.sleep(1000)
        processId = getProcessId(callback)
        if (processId == null) {
            println("process was killed")
            return
        }
        throw RuntimeException("unable to stop app")
    }

    constructor(extension: HomeKeeperExtension) : this() {
        this.extension = extension
        distDir = project.layout.buildDirectory.file("dist").get().asFile
        sshFacade = SshFacade(extension.sshConfig)
        group = CreateDistTask.GROUP_NAME
    }
}

open class UninstallTask : BaseUpdateTask {
    @Inject
    constructor(extension: HomeKeeperExtension) : super(extension)

    @TaskAction
    fun execute() {
        sshFacade.withSession {
            println("uninstalling")
            stopApp(this)
            if(exists("/etc/init.d/home-keeper")){
                executeCommand("sudo update-rc.d home-keeper remove")
                executeCommand("sudo rm -f /etc/init.d/home-keeper")
            }
            if (exists(extension.appConfig.rootDirectory)) {
                println("deleting root directory")
                delete(extension.appConfig.rootDirectory)
            }
            println("application is uninstalled")
        }
    }
    companion object {
        fun getTaskName(project: Project): String {
            return "${project.name}-hk-uninstall"
        }
    }

}
open class InstallTask : BaseUpdateTask {
    @Inject
    constructor(extension: HomeKeeperExtension) : super(extension) {
        dependsOn(CreateDistTask.getTaskName(project))
    }


    @TaskAction
    fun execute() {
        sshFacade.withSession {
            stopApp(this)
            if (exists(extension.appConfig.rootDirectory)) {
                println("deleting root directory")
                delete(extension.appConfig.rootDirectory)
            }
            println("creating app root directory")
            mkDir(extension.appConfig.rootDirectory)
            println("creating app lib directory")
            mkDir("${extension.appConfig.rootDirectory}/lib")
            println("creating app config directory")
            mkDir("${extension.appConfig.rootDirectory}/config")
            println("uploading launch script")
            upload(extension.appConfig.rootDirectory, File(distDir, "home-keeper-startup.sh"))
            executeCommand("sudo chmod +x ${extension.appConfig.rootDirectory}/home-keeper-startup.sh")
            println("uploading shutdown script")
            upload(extension.appConfig.rootDirectory, File(distDir, "home-keeper-shutdown.sh"))
            executeCommand("sudo chmod +x ${extension.appConfig.rootDirectory}/home-keeper-shutdown.sh")
            println("uploading initd script")
            upload(extension.appConfig.rootDirectory, File(distDir, "home-keeper"))
            executeCommand("sudo chmod +x ${extension.appConfig.rootDirectory}/home-keeper")
            if(exists("/etc/init.d/home-keeper")){
                executeCommand("sudo update-rc.d home-keeper remove")
                executeCommand("sudo rm -f /etc/init.d/home-keeper")
            }
            executeCommand("sudo mv ${extension.appConfig.rootDirectory}/home-keeper /etc/init.d/home-keeper")
            if(false) {
                executeCommand("sudo update-rc.d home-keeper defaults")
            }
            println("copying config files")
            File(distDir, "config").listFiles()?.forEach {
                upload("${extension.appConfig.rootDirectory}/config", it)
            }
            println("copying lib files")
            File(distDir, "lib").listFiles()?.forEach {
                upload("${extension.appConfig.rootDirectory}/lib", it)
            }
            println("uploading jdk")
            upload(extension.appConfig.rootDirectory,File(distDir.parentFile, "temp/jdk/jdk.tar.gz"))
            println("extracting jdk")
            executeCommand("tar -zxvf  ${extension.appConfig.rootDirectory}/jdk.tar.gz -C ${extension.appConfig.rootDirectory}")
            delete("${extension.appConfig.rootDirectory}/jdk.tar.gz")
            val fileName = executeCommand("ls ${extension.appConfig.rootDirectory}")!!.lines().first { it.startsWith("amazon-corretto") }
            executeCommand("mv ${extension.appConfig.rootDirectory}/$fileName ${extension.appConfig.rootDirectory}/jre")
            if(false) {
                executeCommand("/etc/init.d/home-keeper start")
            }
        }
        println("application is installed")
    }

    companion object {
        fun getTaskName(project: Project): String {
            return "${project.name}-hk-install"
        }
    }
}

open class UpdateAppTask : BaseUpdateTask {
    @Inject
    constructor(extension: HomeKeeperExtension) : super(extension) {
        dependsOn(CreateDistTask.getTaskName(project))
    }

    @TaskAction
    fun execute() {
        sshFacade.withSession {
            println("updating app")
            if (!exists(extension.appConfig.rootDirectory)) {
                throw Exception("root directory does not exist")
            }
            val localFiles = hashMapOf<String,String>()
            File(distDir, "lib").listFiles()!!.forEach {
                localFiles[it.name] = md5Hash(it)
            }
            val remoteFiles = hashMapOf<String,String>()
            executeCommand("ls ${extension.appConfig.rootDirectory}/lib")!!.lines().filter { it.isNotEmpty() }.forEach {
                remoteFiles[it] = executeCommand("md5sum ${extension.appConfig.rootDirectory}/lib/${it}")!!.substringBefore(" ").trim()
            }
            val toDelete = HashSet(remoteFiles.keys)
            val toUpload = hashSetOf<String>()
            localFiles.entries.forEach {
                if(remoteFiles[it.key] == it.value){
                    toDelete.remove(it.key)
                    return@forEach
                }
                toUpload.add(it.key)
            }
            println("to upload: $toUpload")
            println("to delete: $toDelete")
            stopApp(this)
            toDelete.forEach {
                delete("${extension.appConfig.rootDirectory}/lib/$it")
            }
            toUpload.forEach{
                upload("${extension.appConfig.rootDirectory}/lib", File(distDir, "lib/$it"))
            }
            upload("${extension.appConfig.rootDirectory}/config", File(distDir, "config").listFiles()!!.first())
            executeCommand("/etc/init.d/home-keeper start")
            println("application is updated")
        }
    }

    companion object {
        fun getTaskName(project: Project): String {
            return "${project.name}-hk-update-app"
        }
    }

}

open class UpdateConfigTask : BaseUpdateTask {
    @Inject
    constructor(extension: HomeKeeperExtension) : super(extension) {
        dependsOn(CreateDistTask.getTaskName(project))
    }

    @TaskAction
    fun execute() {
        sshFacade.withSession {
            println("updating config")
            if (!exists(extension.appConfig.rootDirectory)) {
                throw Exception("root directory does not exist")
            }
            upload("${extension.appConfig.rootDirectory}/config", File(distDir, "config").listFiles()!!.first())
            println("config is updated")
        }
    }

    companion object {
        fun getTaskName(project: Project): String {
            return "${project.name}-hk-update-config"
        }
    }

}