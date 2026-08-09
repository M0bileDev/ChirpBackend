package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.domain.exception.InvalidCredentialsException
import com.mobidev.studio.chirp.domain.exception.UserAlreadyExistsException
import com.mobidev.studio.chirp.domain.model.AuthenticatedUser
import com.mobidev.studio.chirp.domain.model.User
import com.mobidev.studio.chirp.infra.database.entities.UserEntity
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.mappers.toUser
import com.mobidev.studio.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
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

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())

        return Base64.getEncoder().encodeToString(hashBytes)
    }
}