package com.mobidev.studio.chirp.domain.model

import com.mobidev.studio.chirp.domain.type.ChatId
import java.util.*

typealias PushNotificationId = String

data class PushNotification(
    val pushNotificationId: PushNotificationId = UUID.randomUUID().toString(),
    val title: String,
    val recipients: List<DeviceToken>,
    val message: String,
    val chatId: ChatId,
    // additional data to push notification service
    val data: Map<String, String>
)
