buildscript{
    dependencies {
        gradleApi()
    }
}
plugins {
    kotlin("jvm") version "2.0.0"
}

tasks.withType<Jar> {
    archiveBaseName.set("home-keeper-mpd")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("com.inthebacklog:javampd:7.2.0")
    implementation(project(":modules:core:app"))
}