package com.mobidev.studio.chirp.api.dto

import com.mobidev.studio.chirp.domain.type.UserId

data class ProfilePictureUpdateDto(
    val userId: UserId,
    val newUrl: String?
)
