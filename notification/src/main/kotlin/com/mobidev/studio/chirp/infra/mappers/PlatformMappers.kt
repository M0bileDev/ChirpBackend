package com.mobidev.studio.chirp.infra.mappers

import com.mobidev.studio.chirp.domain.model.Platform
import com.mobidev.studio.chirp.infra.database.entities.PlatformEntity

fun Platform.toPlatformEntity(): PlatformEntity {
    return when (this) {
        Platform.ANDROID -> PlatformEntity.ANDROID
        Platform.IOS -> PlatformEntity.IOS
    }
}

fun PlatformEntity.toPlatform(): Platform {
    return when (this) {
        PlatformEntity.ANDROID -> Platform.ANDROID
        PlatformEntity.IOS -> Platform.IOS
    }
}