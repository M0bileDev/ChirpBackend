package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.domain.exception.UserAlreadyExistsException
import com.mobidev.studio.chirp.domain.model.User
import com.mobidev.studio.chirp.infra.database.entities.UserEntity
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import com.mobidev.studio.chirp.infra.mappers.toUser
import com.mobidev.studio.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Throws(UserAlreadyExistsException::class)
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
}