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

package ru.vga.hk.zigbee.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.extensions.core.extra
import kotlin.reflect.KProperty

open class HomeKeeperZigbeePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.dependencies.add("implementation", target.dependencies.project(hashMapOf("path" to  ":modules:zigbee:app")))
        target.extensions.create(
            "zigBee",
            HomeKeeperZigBeeExtension::class.java
        )
        val ext = target.extensions.getByName("homeKeeper")
        (ext::class.members.find { it.name == "postInstallHooks" } as KProperty<MutableList<Runnable>>).getter.call(ext).add{
            println("zig bee post install")
        }
        (ext::class.members.find { it.name == "postUpdateHooks" }as KProperty<MutableList<Runnable>>).getter.call(ext).add{
            println("zig bee post update")
        }
        target.tasks.create("${target.name}-delete-node"){
            it.group = "home-keeper"
            it.doLast {
                deleteNode(it.project)
            }
        }
        target.tasks.create("${target.name}-install-node"){
            it.group = "home-keeper"
            it.doLast {
                installNode(it.project)
            }
        }
        target.tasks.create("${target.name}-delete-comqtt"){
            it.group = "home-keeper"
            it.doLast {
                deleteComqtt(it.project)
            }
        }
        target.tasks.create("${target.name}-install-comqtt"){
            it.group = "home-keeper"
            it.doLast {
                installComqtt(it.project, target.extensions.getByType(HomeKeeperZigBeeExtension::class.java))
            }
        }
        target.tasks.create("${target.name}-delete-zigbee2mqtt"){
            it.group = "home-keeper"
            it.doLast {
                deleteZigbee2mqtt(it.project)
            }
        }
        target.tasks.create("${target.name}-install-zigbee2mqtt"){
            it.group = "home-keeper"
            it.doLast {
                installZigbee2mqtt(it.project, target.extensions.getByType(HomeKeeperZigBeeExtension::class.java))
            }
        }
    }
}