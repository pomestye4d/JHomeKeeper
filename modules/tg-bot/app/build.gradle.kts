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
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation(project(":modules:core:app"))
}