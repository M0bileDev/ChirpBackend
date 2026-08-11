package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.domain.exception.InvalidCredentialsException
import com.mobidev.studio.chirp.domain.exception.InvalidTokenException
import com.mobidev.studio.chirp.domain.exception.UserAlreadyExistsException
import com.mobidev.studio.chirp.domain.exception.UserNotFoundException
import com.mobidev.studio.chirp.domain.model.*
import com.mobidev.studio.chirp.infra.database.entities.RefreshTokenEntity
import com.mobidev.studio.chirp.infra.database.entities.UserEntity
import com.mobidev.studio.chirp.infra.database.repositories.RefreshTokenRepository
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.mappers.toUser
import com.mobidev.studio.chirp.infra.security.PasswordEncoder
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
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

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim())
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password, user.password))
            throw InvalidCredentialsException()

        //TODO: check email verification

        val tokens = user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId = userId)
            val refreshToken = jwtService.generateRefreshToken(userId = userId)

            storeRefreshToken(userId = user.id!!, refreshToken)

            Tokens(accessToken = accessToken, refreshToken = refreshToken)
        } ?: throw UserNotFoundException()

        val authenticatedUser = AuthenticatedUser(
            user = user.toUser(),
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken
        )

        return authenticatedUser
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException(
                message = "Invalid refresh token"
            )
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        val hashedRefreshToken = hashToken(refreshToken)
        return user.id?.let { userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashedRefreshToken
            ) ?: throw InvalidTokenException("Invalid refresh token")

            // rotate tokens, remove the old one and create a new pair of access and refresh token
            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashedRefreshToken
            )

            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId = userId, token = newRefreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newRefreshToken,
                refreshToken = newAccessToken
            )

        } ?: throw UserNotFoundException()
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

private data class Tokens(val accessToken: AccessToken, val refreshToken: RefreshToken)