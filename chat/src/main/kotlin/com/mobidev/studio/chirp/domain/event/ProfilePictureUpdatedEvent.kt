package com.mobidev.studio.chirp.domain.event

import com.mobidev.studio.chirp.domain.type.UserId

data class ProfilePictureUpdatedEvent(
    val userId: UserId,
    val newUrl: String?
)