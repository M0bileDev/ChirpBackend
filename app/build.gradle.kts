import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("chirp.spring-boot-app")
}

group = "com.mobidev.studio"
version = "0.0.1-SNAPSHOT"

tasks {
    // configure bootJar task to consider resources from different modules
    named<BootJar>("bootJar"){

        //merge content resources into final executable Java file
        from(project(":notification").projectDir.resolve("src/main/resources")){
            // empty string means -> into root
            into("")
        }
        from(project(":user").projectDir.resolve("src/main/resources")){
            // empty string means -> into root
            into("")
        }
    }
}

dependencies {
    implementation(projects.common)
    implementation(projects.chat)
    implementation(projects.notification)
    implementation(projects.user)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)

    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.redis)

    // message queen dependency
    implementation(libs.spring.boot.starter.amqp)

    // mailgun dependencies
    implementation(libs.spring.boot.starter.mail)
}
