package com.mobidev.studio.chirp.domain.model

import com.mobidev.studio.chirp.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePicture: String?
)
