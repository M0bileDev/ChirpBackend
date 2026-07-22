plugins {
    kotlin("jvm") version "2.3.21"
}

group = "com.mobidev.studio"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}