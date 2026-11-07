package com.mobidev.studio.chirp.infra.message_queue

import com.mobidev.studio.chirp.service.PushNotificationService
import org.springframework.stereotype.Component

@Component
class NotificationChatEventListener(
    private val pushNotificationService: PushNotificationService
)