package com.mobidev.studio.chirp.domain.model

typealias AccessToken = String
typealias RefreshToken = String

data class AuthenticatedUser(
    val user: User,
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
)
