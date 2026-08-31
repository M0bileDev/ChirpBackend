package com.mobidev.studio.chirp.api.dto

import com.mobidev.studio.chirp.domain.type.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean,
)
