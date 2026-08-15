package com.mobidev.studio.chirp.api.controller

import com.mobidev.studio.chirp.api.dto.AuthenticatedUserDto
import com.mobidev.studio.chirp.api.dto.UserDto
import com.mobidev.studio.chirp.api.mappers.toAuthenticatedUserDto
import com.mobidev.studio.chirp.api.mappers.toUserDto
import com.mobidev.studio.chirp.api.request.LoginRequest
import com.mobidev.studio.chirp.api.request.RefreshRequest
import com.mobidev.studio.chirp.api.request.RegisterRequest
import com.mobidev.studio.chirp.service.auth.AuthService
import com.mobidev.studio.chirp.service.auth.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService
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

    @PostMapping("/logout")
    fun logout(
        @RequestBody body: RefreshRequest
    ) {
        authService.logout(body.refreshToken)
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationService.verifyEmail(token)
    }
}