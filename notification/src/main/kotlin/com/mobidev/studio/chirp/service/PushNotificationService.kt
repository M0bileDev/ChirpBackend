package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.domain.exception.InvalidDeviceTokenException
import com.mobidev.studio.chirp.domain.model.DeviceToken
import com.mobidev.studio.chirp.domain.model.Platform
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.entities.DeviceTokenEntity
import com.mobidev.studio.chirp.infra.database.repositories.DeviceTokenRepository
import com.mobidev.studio.chirp.infra.mappers.toDeviceToken
import com.mobidev.studio.chirp.infra.mappers.toPlatformEntity
import com.mobidev.studio.chirp.infra.push_notification.FirebasePushNotificationService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService,
) {

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
}