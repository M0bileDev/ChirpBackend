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
    testImplementation(kotlin("test"))

    //RabbitMQ dependencies
    implementation(libs.spring.boot.starter.amqp)
}

tasks.test {
    useJUnitPlatform()
}