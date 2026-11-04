package com.mobidev.studio.chirp.domain.model

data class PushNotificationSendResult(
    val succeeded: List<DeviceToken>,
    val temporaryFailures: List<DeviceToken>,
    val permanentFailures: List<DeviceToken>,
)
