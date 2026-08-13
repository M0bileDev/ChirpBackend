package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.domain.exception.UserNotFoundException
import com.mobidev.studio.chirp.domain.model.EmailVerificationToken
import com.mobidev.studio.chirp.infra.database.entities.EmailVerificationTokenEntity
import com.mobidev.studio.chirp.infra.database.repositories.EmailVerificationTokenRepository
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.mappers.toEmailVerificationToken
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.concurrent.TimeUnit

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
        val existingTokens = emailVerificationTokenRepository.findByUserAndUsedAtIsNull(
            user = userEntity
        )

        val now = Instant.now()
        val usedTokens = existingTokens.map { it.apply { this.usedAt = now } }
        emailVerificationTokenRepository.saveAll(usedTokens)

        val tokenEntity = EmailVerificationTokenEntity(
            expiresAt = now.plus(emailExpiryHours, ChronoUnit.HOURS),
            user = userEntity
        )

        return emailVerificationTokenRepository.save(tokenEntity).toEmailVerificationToken()
    }
}