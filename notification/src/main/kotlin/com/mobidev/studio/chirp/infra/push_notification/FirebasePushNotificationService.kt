package com.mobidev.studio.chirp.infra.push_notification

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import com.mobidev.studio.chirp.domain.mappers.toSendResult
import com.mobidev.studio.chirp.domain.model.Platform
import com.mobidev.studio.chirp.domain.model.PushNotification
import com.mobidev.studio.chirp.domain.model.PushNotificationSendResult
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

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

    private fun Message.Builder.configOS(
        threadId: String
    ) {
        val aps = Aps.builder()
            .setSound(IOS_NOTIFICATION_SOUND_DEFAULT)
            .setThreadId(threadId)
            .build()
        val apnsConfig = ApnsConfig.builder()
            .setAps(aps)
            .build()

        setApnsConfig(apnsConfig)
    }

    fun sendNotification(
        pushNotification: PushNotification
    ): PushNotificationSendResult {

        val chatIdKey = pushNotification.chatId.toString()
        val messages = pushNotification.recipients
            .map { recipient ->
                val notification = Notification.builder()
                    .setTitle(pushNotification.title)
                    .setBody(pushNotification.message)
                    .build()

                Message.builder()
                    .setToken(recipient.token)
                    .setNotification(notification)
                    .apply {
                        pushNotification.data.forEach { (key, value) ->
                            putData(key, value)
                        }

                        when (recipient.platform) {
                            Platform.ANDROID -> configAndroid(
                                collapsedKey = chatIdKey
                            )

                            Platform.IOS -> configOS(
                                threadId = chatIdKey
                            )
                        }

                    }.build()
            }

        // which push notification failed/succeeded with reason
        val response = FirebaseMessaging.getInstance()
            .sendEach(messages)

        return response.toSendResult(
            allDeviceTokens = pushNotification.recipients,
            logger = logger
        )
    }

    companion object {
        private const val PACKAGE_NAME = "com.mobidev.studio.chirp"
        private const val IOS_NOTIFICATION_SOUND_DEFAULT = "default"
    }
}