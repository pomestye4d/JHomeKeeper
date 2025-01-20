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
        create("home-keeper-tg"){
            id = "home-keeper-tg"
            implementationClass = "ru.vga.hk.tg.gradle.plugin.HomeKeeperTgPlugin"
        }
    }
}
