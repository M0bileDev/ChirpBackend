package com.mobidev.studio.chirp.api.dto

import com.mobidev.studio.chirp.domain.type.UserId

data class ChatParticipantDto(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
