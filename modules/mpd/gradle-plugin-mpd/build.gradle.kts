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
        create("home-keeper-mpd"){
            id = "home-keeper-mpd"
            implementationClass = "ru.vga.hk.mpd.gradle.plugin.HomeKeeperMpdPlugin"
        }
    }
}
