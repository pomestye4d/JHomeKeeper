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
        create("home-keeper-iradio"){
            id = "home-keeper-iradio"
            implementationClass = "ru.vga.hk.iradio.gradle.plugin.HomeKeeperIRadioPlugin"
        }
    }
}
