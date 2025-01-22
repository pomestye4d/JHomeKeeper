plugins {
    kotlin("jvm") version "1.9.24"
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
}


gradlePlugin {
    plugins {
        create("home-keeper-zigbee"){
            id = "home-keeper-zigbee"
            implementationClass = "ru.vga.hk.zigbee.gradle.plugin.HomeKeeperZigbeePlugin"
        }
    }
}
