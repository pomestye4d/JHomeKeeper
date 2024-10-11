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

import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.security.MessageDigest


fun ensureDirectoryExists(dir: File): File {
    if(!dir.exists() && !dir.mkdirs()){
        throw Exception("Could not create directory: $dir");
    }
    return dir
}

fun downloadFile(url: String, dest: File) {
    val url = URI(url).toURL()
    dest.outputStream().use {os ->
        url.openStream().use { ins->
            ins.copyTo(os, 256)
        }
    }
}
fun emptyDir(dir:File):File{
    if(dir.exists()) {
        dir.deleteRecursively()
    }
    return ensureDirectoryExists(dir)
}

fun md5Hash(file: File): String {
    val digest = MessageDigest.getInstance("MD5")
    val fis = FileInputStream(file)

    val byteArray = ByteArray(1024)
    var bytesCount = 0

    while ((fis.read(byteArray).also { bytesCount = it }) != -1) {
        digest.update(byteArray, 0, bytesCount)
    }

    fis.close()

    val bytes = digest.digest()

    val sb = StringBuilder()
    for (i in bytes.indices) {
        sb.append(((bytes[i].toInt() and 0xff) + 0x100).toString(16).substring(1))
    }
    return sb.toString();
}
