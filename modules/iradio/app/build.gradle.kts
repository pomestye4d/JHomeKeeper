buildscript{
    dependencies {
        gradleApi()
    }
}
plugins {
    kotlin("jvm") version "1.9.24"
}

tasks.withType<Jar> {
    archiveBaseName.set("home-keeper-iradio")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation(project(":modules:core:app"))
}