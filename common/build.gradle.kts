plugins {
    // makes it as regular library
    id("java-library")
    id("org.springframework.boot")
    id("chirp.kotlin-common")
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