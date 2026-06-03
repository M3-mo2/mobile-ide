rootProject.name = "mobile-ide"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")
include(":editor-core")
include(":editor-ui")
include(":editor-files")
include(":editor-search")
include(":editor-highlight")
