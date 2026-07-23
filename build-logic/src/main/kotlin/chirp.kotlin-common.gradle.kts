import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    // reference plugin by id -> root level gradle
    kotlin("jvm")
    // springboot related functionalities
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
}

configure<KotlinJvmProjectExtension>{
    jvmToolchain(21)
}