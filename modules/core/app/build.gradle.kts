import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript{
    dependencies {
        gradleApi()
    }
}

plugins {
    kotlin("jvm") version "2.0.20"
}

repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileKotlin.destinationDirectory.set(compileJava.destinationDirectory)

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.8")
}