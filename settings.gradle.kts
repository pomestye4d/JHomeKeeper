rootProject.name = "home-keeper"
include("modules:core:app")
include("modules:tg-bot:app")
include("modules:mpd:app")
include("modules:zigbee:app")
include("modules:iradio:app")
include("modules:babel")
include("web-ui")
include("demo")
include("pomestye4d:hall")
pluginManagement {
    includeBuild("modules/core/gradle-plugin-core")
    includeBuild("modules/tg-bot/gradle-plugin-tg")
    includeBuild("modules/mpd/gradle-plugin-mpd")
    includeBuild("modules/zigbee/gradle-plugin-zigbee")
    includeBuild("modules/iradio/gradle-plugin-iradio")
}