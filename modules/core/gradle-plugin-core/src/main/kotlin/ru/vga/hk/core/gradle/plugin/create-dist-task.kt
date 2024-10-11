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
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

open class CreateDistTask() : DefaultTask() {
    private lateinit var extension: HomeKeeperExtension

    private lateinit var buildDir:File

    @Inject
    constructor(extension: HomeKeeperExtension) : this() {
        this.extension = extension
        buildDir = project.layout.buildDirectory.asFile.get()
        group = GROUP_NAME
        dependsOn("jar")
    }

    @TaskAction
    fun execute() {
        println("creating dist")
        val jdkDir = ensureDirectoryExists( File(buildDir, "temp/jdk"))
        val jdkDist =File(jdkDir, "jdk.tar.gz")
        if(!jdkDist.exists()) {
            println("downloading jdk")
            when(extension.javaConfig.dist){
                JDK_DIST.NIX_64 -> downloadFile("https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.tar.gz", jdkDist)
                JDK_DIST.NIX_AARCH64 -> downloadFile("https://corretto.aws/downloads/latest/amazon-corretto-17-aarch64-linux-jdk.tar.gz", jdkDist)
            }
        }
        println("creating libs")
        val distDir = emptyDir(File(buildDir, "dist"))
        val libsDir = ensureDirectoryExists(File(distDir, "lib"))
        project.configurations.getByName("home-keeper").files.forEach {
            it.copyTo(File(libsDir, it.name))
        }
        println("creating config")
        val configDir = emptyDir(File(distDir, "config"))
        val jar = (project.tasks.getByName("jar") as Jar).outputs.files.first()
        val jarName = "config-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))}.jar"
        jar.copyTo(File(configDir, jarName))
        println("creating launcher")
        val launcher = File(distDir, "home-keeper-startup.sh")
        launcher.writeText(this::class.java.classLoader.getResource("startup.sh").readText().replace("\${rootDir}", "${extension.appConfig.rootDirectory}"))
        launcher.setExecutable(true)
        println("creating shutdown script")
        val shutdownScript = File(distDir, "home-keeper-shutdown.sh")
        shutdownScript.writeText(this::class.java.classLoader.getResource("shutdown.sh").readText().replace("\${rootDir}", "${extension.appConfig.rootDirectory}"))
        shutdownScript.setExecutable(true)
        println("creating init d script")
        val initdScript = File(distDir, "home-keeper")
        initdScript.writeText(this::class.java.classLoader.getResource("initd.sh").readText().replace("\${rootDir}", "${extension.appConfig.rootDirectory}"))
        initdScript.setExecutable(true)
        println("distribution was created")
    }

    companion object {
        fun getTaskName(project: Project): String {
            return "${project.name}-hk-create-dist"
        }
        const val GROUP_NAME = "home-keeper"
    }
}