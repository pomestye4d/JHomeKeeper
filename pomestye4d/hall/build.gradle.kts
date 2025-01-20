plugins {
    kotlin("jvm") version "1.9.24"
    id("home-keeper-core")
    id("home-keeper-tg")
}
homeKeeper{
    ssh {
        host=project.property("ssh.host") as String
        login=project.property("ssh.login") as String
        password=project.property("ssh.password") as String
    }
    jvm {
        timezone="Europe/Moscow"
    }
}
repositories {
    mavenCentral()
}

sourceSets.main {
    java.srcDirs("app/scripts")
    resources.srcDirs("app/config")
}

