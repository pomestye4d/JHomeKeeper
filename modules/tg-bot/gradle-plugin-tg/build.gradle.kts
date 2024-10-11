plugins {
    kotlin("jvm") version "2.0.0"
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
