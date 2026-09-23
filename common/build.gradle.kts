plugins {
    // makes it as regular library
    id("java-library")
    id("chirp.kotlin-common")
}

group = "com.mobidev.studio"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kotlin.reflect)
    api(libs.jackson.module.kotlin)

    testImplementation(kotlin("test"))

    //RabbitMQ dependencies
    implementation(libs.spring.boot.starter.amqp)

    // provides for SecurityContextHolder
    implementation(libs.spring.boot.starter.security)
}

tasks.test {
    useJUnitPlatform()
}