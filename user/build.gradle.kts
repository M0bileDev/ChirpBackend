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
    implementation(projects.common)

    implementation(libs.spring.boot.starter.data.jpa)
    // enable connection to postgresql instance
    runtimeOnly(libs.postgresql)
    // enables password encoder
    implementation(libs.spring.boot.starter.security)
    // enables validation annotations
    implementation(libs.spring.boot.starter.validation)

    // enables jwt related classes
    implementation(libs.jwt.api)
    runtimeOnly(libs.jwt.impl)
    // enables serialization
    runtimeOnly(libs.jwt.jackson)
    // redis template
    implementation(libs.spring.boot.starter.redis)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}