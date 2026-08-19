package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.infra.database.repositories.PasswordResetTokenRepository
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PasswordResetService(
    val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}") private val passwordExpiryMinutes: Long
) {

}