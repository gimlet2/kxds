plugins {
    kotlin("jvm") version "2.2.21" apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.1.0"))
    }
}

group = "org.restmonkeys"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
