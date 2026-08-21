plugins {
    id("chirp.spring-boot-app")
}

group = "com.mobidev.studio"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(projects.common)
    implementation(projects.chat)
    implementation(projects.notification)
    implementation(projects.user)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)

    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.redis)

}
