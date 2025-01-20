plugins {
    kotlin("jvm") version "1.9.24"
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("com.jcraft:jsch:0.1.55")
    implementation("ch.qos.logback:logback-classic:1.5.8")
    implementation(gradleApi())
}


gradlePlugin {
    plugins {
        create("home-keeper-core"){
            id = "home-keeper-core"
            implementationClass = "ru.vga.hk.core.gradle.plugin.HomeKeeperCorePlugin"
        }
    }
}
