package com.mobidev.studio.chirp.infra.mappers

import com.mobidev.studio.chirp.domain.model.DeviceToken
import com.mobidev.studio.chirp.infra.database.entities.DeviceTokenEntity

fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        userId = userId,
        token = token,
        platform = platform.toPlatform(),
        createdAt = createdAt,
        deviceId = id
    )
}