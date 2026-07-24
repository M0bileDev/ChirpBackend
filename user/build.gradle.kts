plugins {
    // makes it as regular library
    id("java-library")
    id("chirp.spring-boot-service")
    // persistence api
    kotlin("plugin.jpa")
}

group = "com.mobidev.studio"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}