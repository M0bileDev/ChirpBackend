package com.mobidev.studio.chirp.infra.mappers

import com.mobidev.studio.chirp.domain.model.EmailVerificationToken
import com.mobidev.studio.chirp.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser()
    )
}