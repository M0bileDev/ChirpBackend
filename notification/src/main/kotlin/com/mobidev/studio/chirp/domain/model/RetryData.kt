package com.mobidev.studio.chirp.domain.model

import java.time.Instant

data class RetryData(
    val pushNotification: PushNotification,
    val attempt: Int,
    val createdAt: Instant
)
