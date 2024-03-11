pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl ("https://artifacts.mercadolibre.com/repository/android-releases/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl ("https://artifacts.mercadolibre.com/repository/android-releases/") }
    }
}

rootProject.name = "Aluno Jiu Jitsu"
include(":app")
 