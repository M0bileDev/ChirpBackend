package com.mobidev.studio.chirp.domain.model

import com.mobidev.studio.chirp.domain.type.UserId
import java.time.Instant

typealias DeviceId = Long

data class DeviceToken(
    val deviceId: DeviceId,
    val userId: UserId,
    val token: String,
    val platform: Platform,
    val createdAt: Instant = Instant.now(),
)
