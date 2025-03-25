pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Cambiar a PREFER_SETTINGS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HELLO"
include(":app")

