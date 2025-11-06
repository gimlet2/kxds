pluginManagement {
    val kotlinVersion: String = "2.2.21"
    val kspVersion: String = "2.2.21-2.0.4"
    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

//plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
//}
rootProject.name = "kxds"
include(":example")
include(":kxds-hosting")

