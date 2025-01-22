buildscript{
    dependencies {
        gradleApi()
    }
}
plugins {
    kotlin("jvm") version "1.9.24"
}

tasks.withType<Jar> {
    archiveBaseName.set("home-keeper-zigbee")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.24")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation(project(":modules:core:app"))
}