pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral() // ✅
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
        maven("https://repository.map.naver.com/archive/maven") // ✅ 여기에 있어야 함!
    }
}
rootProject.name = "Byway"
include(":app")
