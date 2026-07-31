package com.mobidev.studio.chirp.api.dto

import com.mobidev.studio.chirp.domain.model.User

data class AuthenticatedUserDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)
