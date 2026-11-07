package com.mobidev.studio.chirp.api.request

import com.mobidev.studio.chirp.api.dto.PlatformDto
import jakarta.validation.constraints.NotBlank

data class RegisterDeviceRequest(
    @field:NotBlank
    val token: String,
    val platform: PlatformDto
)
