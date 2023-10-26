pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "kotlin-grpc-playground"

include(
    ":proto",
    ":stub",
    ":server",
    ":client",
)