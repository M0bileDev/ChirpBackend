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
    // rabbitMQ dependencies
    implementation(libs.spring.boot.starter.amqp)
    // thymeleaf html template engine dependencies
    implementation(libs.spring.boot.starter.thymeleaf)
    // javaMailSender dependencies
    implementation(libs.spring.boot.starter.mail)
    // firebase app initializer
    implementation(libs.firebase.admin.sdk)
    // data access layer
    implementation(libs.spring.boot.starter.data.jpa)
    // enable connection to postgresql instance
    runtimeOnly(libs.postgresql)
}

tasks.test {
    useJUnitPlatform()
}