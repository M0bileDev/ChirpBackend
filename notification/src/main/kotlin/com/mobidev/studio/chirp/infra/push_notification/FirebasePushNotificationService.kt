package com.mobidev.studio.chirp.infra.push_notification

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class FirebasePushNotificationService(
    @param:Value("\${firebase.credentials-path}") private val firebaseCredentialsPath: String,
    // tool used for loading json content from credentials path
    private val resourceLoader: ResourceLoader
) {
    private val logger = LoggerFactory.getLogger(FirebasePushNotificationService::class.java)

    // initialize function will be called when SpringBoot backend will be properly initialize (called automatically)
    @PostConstruct
    fun initialize() {
        try {
            val serviceAccount = resourceLoader.getResource(firebaseCredentialsPath)
            val credentialsStream = serviceAccount.inputStream
            val googleCredentials = GoogleCredentials.fromStream(credentialsStream)
            val options = FirebaseOptions
                .builder()
                .setCredentials(googleCredentials)
                .build()

            FirebaseApp.initializeApp(options)
            logger.info("Firebase Admin SDK initialized successfully.")
        } catch (ex: Exception) {
            logger.error("Firebase Admin SDK is not initialized!", ex)
            throw ex
        }
    }
}