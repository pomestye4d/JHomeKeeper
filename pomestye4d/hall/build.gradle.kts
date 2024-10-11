plugins {
    kotlin("jvm") version "2.0.0"
    id("home-keeper-core")
    id("home-keeper-tg")
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

