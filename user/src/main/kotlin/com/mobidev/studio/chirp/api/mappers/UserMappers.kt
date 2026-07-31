package com.mobidev.studio.chirp.api.mappers

import com.mobidev.studio.chirp.api.dto.UserDto
import com.mobidev.studio.chirp.domain.model.User

fun User.toUserDto(): UserDto {
    return UserDto(
        id = this.id,
        username = this.username,
        email = this.email,
        hasVerifiedEmail = this.hasEmailVerified
    )
}