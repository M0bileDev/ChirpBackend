package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.infra.database.entities.PasswordResetTokenEntity
import com.mobidev.studio.chirp.infra.database.repositories.PasswordResetTokenRepository
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}") private val passwordExpiryMinutes: Long
) {

    fun requestPasswordReset(email: String){
        val user = userRepository.findByEmail(email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val passwordResetToken = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(passwordExpiryMinutes, ChronoUnit.MINUTES)
        )
        passwordResetTokenRepository.save(passwordResetToken)

        // TODO: trigger notification service to send reset password email
    }
}