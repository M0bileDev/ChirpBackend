package com.mobidev.studio.chirp.api.controller

import com.mobidev.studio.chirp.api.dto.AuthenticatedUserDto
import com.mobidev.studio.chirp.api.dto.UserDto
import com.mobidev.studio.chirp.api.mappers.toAuthenticatedUserDto
import com.mobidev.studio.chirp.api.mappers.toUserDto
import com.mobidev.studio.chirp.api.request.LoginRequest
import com.mobidev.studio.chirp.api.request.RefreshRequest
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

    @PostMapping("/login")
    fun login(
        @RequestBody body: LoginRequest,
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return authService.refresh(
            body.refreshToken
        ).toAuthenticatedUserDto()
    }
}