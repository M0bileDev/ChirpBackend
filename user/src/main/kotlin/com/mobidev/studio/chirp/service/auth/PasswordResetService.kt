package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.domain.events.user.UserEvent
import com.mobidev.studio.chirp.domain.exception.InvalidCredentialsException
import com.mobidev.studio.chirp.domain.exception.InvalidTokenException
import com.mobidev.studio.chirp.domain.exception.SamePasswordException
import com.mobidev.studio.chirp.domain.exception.UserNotFoundException
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.entities.PasswordResetTokenEntity
import com.mobidev.studio.chirp.infra.database.repositories.PasswordResetTokenRepository
import com.mobidev.studio.chirp.infra.database.repositories.RefreshTokenRepository
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.message_queue.EventPublisher
import com.mobidev.studio.chirp.infra.security.PasswordEncoder
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}") private val passwordExpiryMinutes: Long,
    private val eventPublisher: EventPublisher
) {

    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val passwordResetToken = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(passwordExpiryMinutes, ChronoUnit.MINUTES)
        )
        passwordResetTokenRepository.save(passwordResetToken)

        eventPublisher.publish(
            event = UserEvent.RequestResetPassword(
                userId = user.id!!,
                email = user.email,
                username = user.username,
                passwordResetToken = passwordResetToken.token,
                expiresInMinutes = passwordExpiryMinutes
            )
        )
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken =
            passwordResetTokenRepository.findByToken(token) ?: throw InvalidTokenException("Invalid password token")

        if (resetToken.isUsed) {
            throw InvalidTokenException("Reset password token is already used.")
        }

        if (resetToken.isExpired) {
            throw InvalidTokenException("Reset password token is expired.")
        }

        val user = resetToken.user
        if (passwordEncoder.matches(newPassword, user.password)) {
            throw SamePasswordException()
        }

        val updatedToken = resetToken.apply { usedAt = Instant.now() }
        passwordResetTokenRepository.save(updatedToken)

        val hashedNewPassword = passwordEncoder.encode(newPassword)
        val updatedUser = user.apply { this.password = hashedNewPassword }
        userRepository.save(updatedUser)

        // invalidate all active sessions
        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String,
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(oldPassword, user.password)) {
            throw InvalidCredentialsException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)

        val newHashedPassword = passwordEncoder.encode(newPassword)
        val updatedUser = user.apply { this.password = newHashedPassword }
        userRepository.save(updatedUser)
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokensTask() {
        val now = Instant.now()

        passwordResetTokenRepository.deleteByExpiresAtLessThan(now)
    }
}