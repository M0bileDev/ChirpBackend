package com.mobidev.studio.chirp.api.mappers

import com.mobidev.studio.chirp.api.dto.AuthenticatedUserDto
import com.mobidev.studio.chirp.api.dto.UserDto
import com.mobidev.studio.chirp.domain.model.AuthenticatedUser
import com.mobidev.studio.chirp.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = this.user.toUserDto(),
        accessToken = this.accessToken,
        refreshToken = this.refreshToken,
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = this.id,
        username = this.username,
        email = this.email,
        hasVerifiedEmail = this.hasEmailVerified
    )
}