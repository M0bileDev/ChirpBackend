pluginManagement {
    // make reference to build-logic project files
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "chirp"

include("app")
include("user")
include("chat")
include("notification")
include("common")