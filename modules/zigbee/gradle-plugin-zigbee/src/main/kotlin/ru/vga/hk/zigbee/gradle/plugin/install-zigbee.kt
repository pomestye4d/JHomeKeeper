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

import org.gradle.api.Project
import java.nio.charset.StandardCharsets

val nodeDistName = "node-v22.13.1-linux-x64"
val nodeDownloadLink = "https://nodejs.org/dist/v22.13.1/${nodeDistName}.tar.xz"
val comqttVersion = "v2.6.0"
val zigbee2mqttVersion = "2.0.0"

fun deleteNode(project: Project) {
    val sshFacade = SshFacade(project)
    sshFacade.withSession {
        if(exists("${sshFacade.rootDirectory}/externalPrograms/node")){
            delete("${sshFacade.rootDirectory}/externalPrograms/node")
        }
        executeCommand("sudo rm /usr/bin/node")
    }
}

fun deleteComqtt(project: Project){
    val sshFacade = SshFacade(project)
    sshFacade.withSession {
        if(exists("${sshFacade.rootDirectory}/externalPrograms/comqtt")){
            delete("${sshFacade.rootDirectory}/externalPrograms/comqtt")
        }
    }
}



fun installNode(project: Project) {
    val sshFacade = SshFacade(project)
    sshFacade.withSession {
        if(!exists("${sshFacade.rootDirectory}/externalPrograms")){
            mkDir("${sshFacade.rootDirectory}/externalPrograms")
        }
        if(!exists("${sshFacade.rootDirectory}/externalPrograms/node")){
            mkDir("${sshFacade.rootDirectory}/externalPrograms/node")
        }
        if(exists("${sshFacade.rootDirectory}/externalPrograms/node/download-url.txt")){
            val url = getContent("${sshFacade.rootDirectory}/externalPrograms/node/download-url.txt").toString(StandardCharsets.UTF_8)
            if(nodeDownloadLink == url){
                println("node is actual (${nodeDownloadLink})")
                return@withSession
            }
            delete("${sshFacade.rootDirectory}/externalPrograms/node")
            mkDir("${sshFacade.rootDirectory}/externalPrograms/node")
        }
        executeCommand("wget -O ${sshFacade.rootDirectory}/externalPrograms/node/node.tar.xz $nodeDownloadLink")
        executeCommand("tar xJf ${sshFacade.rootDirectory}/externalPrograms/node/node.tar.xz -C ${sshFacade.rootDirectory}/externalPrograms/node")
        executeCommand("mv ${sshFacade.rootDirectory}/externalPrograms/node/${nodeDistName} ${sshFacade.rootDirectory}/externalPrograms/node/dist")
        upload("${sshFacade.rootDirectory}/externalPrograms/node/download-url.txt", nodeDownloadLink.toByteArray())
        if(exists("/usr/bin/node")){
            executeCommand("sudo rm /usr/bin/node")
        }
        executeCommand("sudo ln ${sshFacade.rootDirectory}/externalPrograms/node/dist/bin/node /usr/bin/node")
        if(!exists("/usr/lib/node_modules")){
            mkDir("/usr/lib/node_modules", sshFacade.sshConfig.login, "777")
        }
        executeCommand("sudo ${sshFacade.rootDirectory}/externalPrograms/node/dist/bin/npm install -g pnpm")
    }
}

fun installComqtt(project: Project, ext: HomeKeeperZigBeeExtension) {
    val sshFacade = SshFacade(project)
    sshFacade.withSession {
        if(!exists("${sshFacade.rootDirectory}/externalPrograms")){
            mkDir("${sshFacade.rootDirectory}/externalPrograms")
        }
        if(!exists("${sshFacade.rootDirectory}/externalPrograms/comqtt")){
            mkDir("${sshFacade.rootDirectory}/externalPrograms/comqtt")
        }
        if(exists("${sshFacade.rootDirectory}/externalPrograms/comqtt/version.txt")){
            val version = getContent("${sshFacade.rootDirectory}/externalPrograms/comqtt/version.txt").toString(StandardCharsets.UTF_8)
            if(comqttVersion == version){
                println("comqtt is actual (${version})")
                return@withSession
            }
            delete("${sshFacade.rootDirectory}/externalPrograms/comqtt")
            mkDir("${sshFacade.rootDirectory}/externalPrograms/comqtt")
        }
        upload("${sshFacade.rootDirectory}/externalPrograms/comqtt/comqtt", this::class.java.classLoader.getResource("comqtt/comqtt").readBytes())
        executeCommand("sudo chmod 777 ${sshFacade.rootDirectory}/externalPrograms/comqtt/comqtt")
        upload("${sshFacade.rootDirectory}/externalPrograms/comqtt/config.yaml", this::class.java.classLoader.getResource("comqtt/single.yml").readBytes())
        if(ext.comqttConfigFile != null){
            delete("${sshFacade.rootDirectory}/externalPrograms/comqtt/config.yaml")
            upload("${sshFacade.rootDirectory}/externalPrograms/comqtt/config.yaml", ext.comqttConfigFile!!.readBytes())
        }
    }
}

fun deleteZigbee2mqtt(project: Project){
    val sshFacade = SshFacade(project)
    sshFacade.withSession {
        if(exists("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt")){
            delete("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt")
        }
    }
}

fun installZigbee2mqtt(project: Project, ext: HomeKeeperZigBeeExtension) {
    val sshFacade = SshFacade(project)
    sshFacade.withSession {
        if(!exists("${sshFacade.rootDirectory}/externalPrograms")){
            mkDir("${sshFacade.rootDirectory}/externalPrograms")
        }
        if(!exists("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt")){
            mkDir("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt")
        }
        if(!exists("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/backup")){
            mkDir("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/backup")
        }
        if(exists("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/version.txt")){
            val url = getContent("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/version.txt").toString(StandardCharsets.UTF_8)
            if(nodeDownloadLink == url){
                println("zigbee2mqtt is actual (${zigbee2mqttVersion})")
                return@withSession
            }
            if(exists("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/backup/data")){
                delete("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist/data")
            }
            if(exists("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/zigbee2mqtt.zip")){
                delete("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/zigbee2mqtt.zip")
            }
            executeCommand("cp ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist/data ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/backup/data")
            delete("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist")
        }
        upload("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/zigbee2mqtt.zip", this::class.java.classLoader.getResource("zigbee2mqtt/zigbee2mqtt.zip").readBytes())
        executeCommand("unzip ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/zigbee2mqtt.zip -d ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt")
        executeCommand("mv ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/zigbee2mqtt-master ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist")
        upload("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/version.txt", zigbee2mqttVersion.toByteArray())
        executeCommand("cd ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist && ../../node/dist/bin/pnpm i --frozen-lockfile")
        if(exists("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/backup/data")){
            if(exists("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist/data")){
                delete("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist/data")
            }
            executeCommand("cp ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/backup/data ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist/data")
        } else if(ext.zigbee2mqttConfigFile != null){
                upload("${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist/data/configuration.yaml", ext.zigbee2mqttConfigFile!!.readBytes())
        } else {
            executeCommand("cp ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist/data/configuration.example.yaml ${sshFacade.rootDirectory}/externalPrograms/zigbee2mqtt/dist/data/configuration.yaml")
        }
    }
}