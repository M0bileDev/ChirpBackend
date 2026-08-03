package com.mobidev.studio.chirp.api.controller

import com.mobidev.studio.chirp.api.dto.UserDto
import com.mobidev.studio.chirp.api.mappers.toUserDto
import com.mobidev.studio.chirp.api.request.RegisterRequest
import com.mobidev.studio.chirp.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest,
    ): UserDto {
        return authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()
    }

}