pluginManagement {
    repositories {
        maven { url = uri("https://maven.google.com") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.google.com") }
        google()
        mavenCentral()
    }
}

rootProject.name = "campus-eats-app-kt"
include(":app")
