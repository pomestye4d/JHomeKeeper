buildscript{
    dependencies {
        gradleApi()
    }
}

plugins {
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.8")
    implementation("org.rrd4j:rrd4j:3.9")
    implementation("com.google.code.gson:gson:2.11.0")

}