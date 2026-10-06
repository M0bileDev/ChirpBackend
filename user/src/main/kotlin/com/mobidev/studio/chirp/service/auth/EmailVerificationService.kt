package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.domain.events.user.UserEvent
import com.mobidev.studio.chirp.domain.exception.InvalidTokenException
import com.mobidev.studio.chirp.domain.exception.UserNotFoundException
import com.mobidev.studio.chirp.domain.model.EmailVerificationToken
import com.mobidev.studio.chirp.infra.database.entities.EmailVerificationTokenEntity
import com.mobidev.studio.chirp.infra.database.repositories.EmailVerificationTokenRepository
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.mappers.toEmailVerificationToken
import com.mobidev.studio.chirp.infra.message_queue.EventPublisher
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.verification.expiry-hours}") private val emailExpiryHours: Long,
    private val eventPublisher: EventPublisher
) {

    @Transactional
    fun createVerificationToken(
        email: String,
    ): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email) ?: throw UserNotFoundException()

        emailVerificationTokenRepository.invalidateActiveTokensForUser(user = userEntity)

        val tokenEntity = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(emailExpiryHours, ChronoUnit.HOURS),
            user = userEntity
        )

        return emailVerificationTokenRepository.save(tokenEntity).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val emailVerificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is invalid.")

        if (emailVerificationToken.isUsed) {
            throw InvalidTokenException("Email verification token is already used.")
        }

        if (emailVerificationToken.isExpired) {
            throw InvalidTokenException("Email verification token is expired.")
        }

        val updatedToken = emailVerificationToken.apply { usedAt = Instant.now() }
        emailVerificationTokenRepository.save(updatedToken)

        val updatedUser = emailVerificationToken.user.apply { hasVerifiedEmail = true }
        userRepository.save(updatedUser)

        val userId = updatedUser.id!!
        val email = updatedUser.email
        val username = updatedUser.username

        // publish that user has verified email (add chat participant in chat service into database table)
        eventPublisher.publish(
            UserEvent.Verified(
                userId = userId,
                email = email,
                username = username
            )
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokensTask() {
        val now = Instant.now()

        emailVerificationTokenRepository.deleteByExpiresAtLessThan(now)
    }

    @Transactional
    fun resendVerificationEmail(email: String) {
        val verificationToken = createVerificationToken(email = email)

        if (verificationToken.user.hasEmailVerified) return

        val eventRequestResendVerification = with(verificationToken.user) {
            UserEvent.RequestResendVerification(
                userId = id,
                email = this.email,
                username = username,
                verificationToken = verificationToken.token
            )
        }
        eventPublisher.publish(event = eventRequestResendVerification)
    }
}