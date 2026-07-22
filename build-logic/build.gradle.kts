plugins {
    // define build specific module
    `kotlin-dsl`
}

repositories {
    // remote sources where the dependencies that will be declare here (in this gradle file)
    // can be found from gradle
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    //dependencies from toml file are not recognized here, so hardcoded
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.3.21")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.1.0")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
}