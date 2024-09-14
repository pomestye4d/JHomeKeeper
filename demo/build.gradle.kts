plugins {
    kotlin("jvm") version "2.0.20"
}

sourceSets.main {
    java.srcDirs("configuration")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":modules:core:app"))
    implementation(project(":modules:tg-bot:app"))
}
tasks.withType<Jar> {
    from("configuration") {
        include ("application.properties")
    }
}
task("localDeploy"){
    group = "build"
    dependsOn("jar")
    doLast {
        project.layout.buildDirectory.file("libs/demo.jar").get().asFile.copyTo(
            project.layout.projectDirectory.file("local-dev/config/conf-${System.currentTimeMillis()}.jar").asFile)
    }
}