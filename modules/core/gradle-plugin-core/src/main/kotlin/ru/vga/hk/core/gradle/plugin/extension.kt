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


@file:Suppress("unused")

package ru.vga.hk.core.gradle.plugin

import org.gradle.api.Project
import javax.inject.Inject

@DslMarker
annotation class HomeKeeperConfigMaker

enum class JDK_DIST{
    NIX_64,
    NIX_AARCH64,
}

@HomeKeeperConfigMaker
open class HomeKeeperExtension @Inject constructor(private val project: Project) {

    internal var sshConfig: SshConfig = SshConfig()

    internal var appConfig: AppConfig = AppConfig()

    internal var javaConfig: JavaConfig = JavaConfig()

    fun app(block: AppConfig.() -> Unit){
        appConfig.block()
    }

    fun ssh(block: SshConfig.() -> Unit){
        sshConfig.block()
    }

    fun java(block: JavaConfig.() -> Unit){
        javaConfig.block()
    }


    fun createTasks(){
        this.project.tasks.create(CreateDistTask.getTaskName(project), CreateDistTask::class.java, this)
        this.project.tasks.create(InstallTask.getTaskName(project), InstallTask::class.java, this)
        this.project.tasks.create(UninstallTask.getTaskName(project), UninstallTask::class.java, this)
        this.project.tasks.create(UpdateAppTask.getTaskName(project), UpdateAppTask::class.java, this)
        this.project.tasks.create(UpdateConfigTask.getTaskName(project), UpdateConfigTask::class.java, this)
    }
}

@HomeKeeperConfigMaker
class SshConfig{
    lateinit var host: String
    lateinit var login: String;
    lateinit var password: String;
}

@HomeKeeperConfigMaker
class AppConfig{
    var rootDirectory = "/usr/local/homekeeper"
}

@HomeKeeperConfigMaker
class JavaConfig{
    var dist = JDK_DIST.NIX_64
}