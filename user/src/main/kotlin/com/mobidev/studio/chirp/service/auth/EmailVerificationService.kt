package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.domain.exception.InvalidTokenException
import com.mobidev.studio.chirp.domain.exception.UserNotFoundException
import com.mobidev.studio.chirp.domain.model.EmailVerificationToken
import com.mobidev.studio.chirp.infra.database.entities.EmailVerificationTokenEntity
import com.mobidev.studio.chirp.infra.database.repositories.EmailVerificationTokenRepository
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.mappers.toEmailVerificationToken
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
    @param:Value("\${chirp.email.verification.expiry-hours}") private val emailExpiryHours: Long
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
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokensTask(){
        val now = Instant.now()

        emailVerificationTokenRepository.deleteByExpiresAtLessThan(now)
    }

    fun resendEmailVerificationEmail(email: String){
        //todo: trigger resend
    }
}