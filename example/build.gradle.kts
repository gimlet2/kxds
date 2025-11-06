plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm") version "2.2.21"

}

group = "org.restmonkeys"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":kxds-hosting"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.2")
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
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
