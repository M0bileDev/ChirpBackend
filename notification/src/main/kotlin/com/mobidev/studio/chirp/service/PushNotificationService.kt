package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.infra.database.repositories.DeviceTokenRepository
import com.mobidev.studio.chirp.infra.push_notification.FirebasePushNotificationService
import org.springframework.stereotype.Service

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService,
)