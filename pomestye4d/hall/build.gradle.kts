plugins {
    kotlin("jvm") version "1.9.24"
    id("home-keeper-core")
    id("home-keeper-tg")
    id("home-keeper-zigbee")
    id("home-keeper-mpd")
}
homeKeeper{
    ssh {
        host=project.property("ssh.host") as String
        login=project.property("ssh.login") as String
        password=project.property("ssh.password") as String
    }
    jvm {
        timezone="Europe/Moscow"
    }
}
zigBee{
    comqttConfigFile = project.file("app/config/comqtt.yml")
    zigbee2mqttConfigFile = project.file("app/config/zigbee2mqtt.yml")
}
repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDirs("app/scripts")
    resources.srcDirs("app/config")
}

