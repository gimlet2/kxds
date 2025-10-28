import java.net.URL

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.bjornvester.xjc") version "1.8.2"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
    }
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.0-1.0.29")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.6")
    implementation("com.squareup:kotlinpoet:2.0.0")
    implementation("io.github.pdvrieze.xmlutil:core:0.90.0-RC3")
    implementation("io.github.pdvrieze.xmlutil:core-jvm:0.90.0-RC3")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.90.0-RC3")
    testImplementation(kotlin("test"))

}

group = "org.restmonkeys"
version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}

xjc {
    xsdDir.file("src/main/resources/schema/schema.xsd")
}

sourceSets.main {
    java.srcDir("build/generated/sources/xjc/java")
}
