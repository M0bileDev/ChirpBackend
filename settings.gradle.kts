pluginManagement {
    // make reference to build-logic project files
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "chirp"

include("app")
include("user")
include("chat")
include("notification")
include("common")