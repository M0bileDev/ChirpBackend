package com.mobidev.studio.chirp.infra.push_notification

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
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

    fun isValidToken(
        token: String,
    ): Boolean {
        val message = Message
            .builder()
            .setToken(token)
            .build()

        return try {
            FirebaseMessaging
                .getInstance()
                .send(
                    message,
                    true
                )
            true
        } catch (ex: FirebaseMessagingException) {
            logger.error("Failed to validate Firebase token!", ex)
            false
        }
    }


    private fun Message.Builder.configAndroid(
        collapsedKey: String
    ) {
        val androidConfig = AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .setCollapseKey(collapsedKey)
            .setRestrictedPackageName(PACKAGE_NAME)
            .build()

        setAndroidConfig(androidConfig)
    }

    companion object {
        private const val PACKAGE_NAME = "com.mobidev.studio.chirp"
    }
}