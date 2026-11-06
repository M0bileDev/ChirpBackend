package com.mobidev.studio.chirp.api.dto

import com.mobidev.studio.chirp.domain.type.UserId
import java.time.Instant

data class DeviceTokenDto(
    val userId: UserId,
    val token: String,
    val createdAt: Instant
)
