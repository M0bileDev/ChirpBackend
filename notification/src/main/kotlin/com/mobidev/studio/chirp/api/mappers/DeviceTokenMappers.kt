package com.mobidev.studio.chirp.api.mappers

import com.mobidev.studio.chirp.api.dto.DeviceTokenDto
import com.mobidev.studio.chirp.domain.model.DeviceToken

fun DeviceToken.toDeviceTokenDto(): DeviceTokenDto {
    return DeviceTokenDto(
        userId = userId,
        token = token,
        createdAt = createdAt,
    )
}