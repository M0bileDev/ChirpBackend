package com.mobidev.studio.chirp.infra.security

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class PasswordEncoder {

    private val bcrypt = BCryptPasswordEncoder()

    fun encode(rawPassword: String): String {
        if (rawPassword.isEmpty()) throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Password must not be empty"
        )
        return bcrypt.encode(rawPassword)!!
    }

    fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return bcrypt.matches(rawPassword, encodedPassword)
    }
}