package com.mobidev.studio.chirp.domain.model

import com.mobidev.studio.chirp.domain.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasEmailVerified: Boolean,
)
