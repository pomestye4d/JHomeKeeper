rootProject.name = "home-keeper"
include("modules:core:app")
include("modules:tg-bot:app")
include("modules:babel")
include("web-ui")
include("demo")
pluginManagement {
    repositories {
        maven {
            name = "local-maven"
            url = uri("local-maven-repository")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}