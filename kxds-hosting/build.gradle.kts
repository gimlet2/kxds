import java.net.URL

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.bjornvester.xjc") version "1.8.2"
    `maven-publish`
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
    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("io.github.pdvrieze.xmlutil:core:0.90.0-RC3")
    implementation("io.github.pdvrieze.xmlutil:core-jvm:0.90.0-RC3")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.90.0-RC3")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    testImplementation(kotlin("test"))

}

group = "org.restmonkeys"
version = "0.1.0"

tasks.test {
    useJUnitPlatform()
}

xjc {
    xsdDir.file("src/main/resources/schema/schema.xsd")
}

sourceSets.main {
    java.srcDir("build/generated/sources/xjc/java")
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().allSource)
    exclude("**/org/w3/**")  // Exclude generated XJC sources
}

tasks.named<Javadoc>("javadoc") {
    options {
        (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
    // Exclude generated XJC sources from javadoc
    exclude("**/org/w3/**")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("KXDS - Kotlin XSD Data Class Generator")
                description.set("A Kotlin Symbol Processing (KSP) plugin that automatically generates Kotlin data classes from XML Schema Definition (XSD) files")
                url.set("https://github.com/gimlet2/kxds")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("gimlet2")
                        name.set("RestMonkeys")
                        organizationUrl.set("https://github.com/gimlet2")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/gimlet2/kxds.git")
                    developerConnection.set("scm:git:ssh://github.com/gimlet2/kxds.git")
                    url.set("https://github.com/gimlet2/kxds")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/gimlet2/kxds")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
