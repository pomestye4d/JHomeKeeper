plugins {
    kotlin("jvm") version "1.9.24"
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("com.jcraft:jsch:0.1.55")
}


gradlePlugin {
    plugins {
        create("home-keeper-zigbee"){
            id = "home-keeper-zigbee"
            implementationClass = "ru.vga.hk.zigbee.gradle.plugin.HomeKeeperZigbeePlugin"
        }
    }
}
