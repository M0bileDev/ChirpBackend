package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.domain.exception.InvalidDeviceTokenException
import com.mobidev.studio.chirp.domain.model.DeviceToken
import com.mobidev.studio.chirp.domain.model.Platform
import com.mobidev.studio.chirp.domain.model.PushNotification
import com.mobidev.studio.chirp.domain.model.RetryData
import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.entities.DeviceTokenEntity
import com.mobidev.studio.chirp.infra.database.repositories.DeviceTokenRepository
import com.mobidev.studio.chirp.infra.mappers.toDeviceToken
import com.mobidev.studio.chirp.infra.mappers.toPlatformEntity
import com.mobidev.studio.chirp.infra.push_notification.FirebasePushNotificationService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentSkipListMap

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // ConcurrentSkipListSet (thread safe) -> summarizes certain values based on the key (in this case timestamp -> Long)
    //  at a given millisecond timestamp certain values will be performed
    private val retryQueue = ConcurrentSkipListMap<Long, MutableList<RetryData>>()

    @Transactional
    fun registerDevice(
        userId: UserId,
        token: String,
        platform: Platform
    ): DeviceToken {
        val existingToken = deviceTokenRepository.findByToken(token)
        val trimmedToken = token.trim()

        if (existingToken == null && !firebasePushNotificationService.isValidToken(trimmedToken)) {
            throw InvalidDeviceTokenException()
        }

        val entity = if (existingToken != null) {
            // update existing token
            val updatedToken = existingToken.apply {
                this.userId = userId
            }
            deviceTokenRepository.save(updatedToken)
        } else {
            val newDeviceToken = DeviceTokenEntity(
                userId = userId,
                token = trimmedToken,
                platform = platform.toPlatformEntity()
            )
            deviceTokenRepository.save(newDeviceToken)
        }

        return entity.toDeviceToken()
    }

    @Transactional
    fun unregisterDevice(
        token: String
    ) {
        val trimmedToken = token.trim()
        deviceTokenRepository.deleteByToken(trimmedToken)
    }

    fun sendNewMessageNotifications(
        recipientUserIds: List<UserId>,
        senderUserId: UserId,
        message: String,
        chatId: ChatId,
        senderUsername: String,
    ) {
        val deviceTokens = deviceTokenRepository.findByUserIdIn(recipientUserIds)
        if (deviceTokens.isEmpty()) {
            logger.info("No device tokens found for $recipientUserIds")
            return
        }

        val recipients = deviceTokens
            .filter { it.userId != senderUserId }
            .map { it.toDeviceToken() }

        val pushNotification = PushNotification(
            title = "New message from $senderUsername",
            recipients = recipients,
            message = message,
            chatId = chatId,
            // additional data read by the client
            data = mapOf(
                CHAT_ID to chatId.toString(),
                TYPE to NEW_MESSAGE
            )
        )

        firebasePushNotificationService.sendNotification(pushNotification)
    }

    fun sendWithRetry(
        pushNotification: PushNotification,
        attempt: Int = 0
    ) {
        val result = firebasePushNotificationService.sendNotification(pushNotification)

        if (result.permanentFailures.isNotEmpty()) {
            // remove device tokens from database as they are not usable anymore (they caused permanent failures)
            result.permanentFailures.forEach { deviceToken ->
                deviceTokenRepository.deleteByToken(token = deviceToken.token)
            }
        }

        // not empty and backoff steps are still available (in range 30..600 seconds)
        if (result.temporaryFailures.isNotEmpty() && attempt < RETRY_DELAYS_SECONDS.size) {
            val retryPushNotification = pushNotification.copy(
                recipients = result.temporaryFailures
            )
            //todo schedule retry
        }

        if(result.succeeded.isNotEmpty()){
            logger.info("Successfully sent notification to ${result.succeeded.size} devices")
        }
    }

    companion object {
        private const val CHAT_ID = "chatId"
        private const val TYPE = "type"
        private const val NEW_MESSAGE = "new_message"
        private val RETRY_DELAYS_SECONDS = listOf(
            30L,
            60L,
            120L,
            300L,
            600L
        )
        private const val MAX_RETRY_AGE_MINUTES = 30L
    }
}