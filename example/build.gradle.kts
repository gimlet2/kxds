plugins {
    id("com.google.devtools.ksp") version "2.3.0"
    kotlin("jvm") version "2.1.0"

}

group = "org.restmonkeys"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":kxds-hosting"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.0-1.0.29")
    ksp(project(":kxds-hosting"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

ksp {
    arg("path", "${layout.projectDirectory}/src/main/resources/xds/")
}
