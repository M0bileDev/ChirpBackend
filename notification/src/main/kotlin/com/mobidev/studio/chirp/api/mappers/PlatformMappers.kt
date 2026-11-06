package com.mobidev.studio.chirp.api.mappers

import com.mobidev.studio.chirp.api.dto.PlatformDto
import com.mobidev.studio.chirp.domain.model.Platform

fun Platform.toPlatformDto(): PlatformDto {
    return when (this) {
        Platform.ANDROID -> PlatformDto.ANDROID
        Platform.IOS -> PlatformDto.IOS
    }
}