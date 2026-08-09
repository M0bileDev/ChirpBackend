package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.domain.exception.UserAlreadyExistsException
import com.mobidev.studio.chirp.domain.model.User
import com.mobidev.studio.chirp.domain.model.UserId
import com.mobidev.studio.chirp.infra.database.entities.RefreshTokenEntity
import com.mobidev.studio.chirp.infra.database.entities.UserEntity
import com.mobidev.studio.chirp.infra.database.repositories.RefreshTokenRepository
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.mappers.toUser
import com.mobidev.studio.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun register(email: String, username: String, password: String): User {
        val user = userRepository.findByEmailOrUsername(
            email = email.trim(),
            username = username.trim()
        )
        if (user != null) throw UserAlreadyExistsException()

        // jpa will create id, createdAt, updatedAt
        val newUser =
            UserEntity(
                email = email.trim(),
                username = username.trim(),
                password = passwordEncoder.encode(password)
            )
        // upsert
        val savedUser = userRepository.save(newUser).toUser()

        return savedUser
    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashedRefreshToken = hashToken(token = token)
        val expiresAt = Instant.now().plusMillis(jwtService.refreshTokenValidityMs)
        val refreshTokenEntity = RefreshTokenEntity(
            userId = userId,
            expiresAt = expiresAt,
            hashedToken = hashedRefreshToken
        )

        refreshTokenRepository.save(
            refreshTokenEntity
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())

        return Base64.getEncoder().encodeToString(hashBytes)
    }
}