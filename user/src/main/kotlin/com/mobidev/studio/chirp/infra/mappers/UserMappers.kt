package com.mobidev.studio.chirp.infra.mappers

import com.mobidev.studio.chirp.domain.model.User
import com.mobidev.studio.chirp.infra.database.entities.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = this.id!!,
        username = this.username,
        email = this.email,
        hasEmailVerified = this.hasVerifiedEmail
    )
}