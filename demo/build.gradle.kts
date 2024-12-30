plugins {
    kotlin("jvm") version "2.0.0"
    id("home-keeper-core")
}
homeKeeper{
    ssh {
        host=project.property("ssh.host") as String
        login=project.property("ssh.login") as String
        password=project.property("ssh.password") as String
    }
}
repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDirs("app/scripts")
    resources.srcDirs("app/config")
}


task("localDeploy"){
    group = "build"
    dependsOn("jar")
    doLast {
        project.layout.buildDirectory.file("libs/demo.jar").get().asFile.copyTo(
            project.layout.projectDirectory.file("local-dev/config/conf-${System.currentTimeMillis()}.jar").asFile)
    }
}