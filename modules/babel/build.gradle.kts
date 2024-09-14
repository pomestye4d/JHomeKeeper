plugins{
    java
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(project(":modules:core:app"))
    implementation(project(":modules:tg-bot:app"))
}