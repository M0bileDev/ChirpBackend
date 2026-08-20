package com.mobidev.studio.chirp.api.request

import jakarta.validation.constraints.Email

data class EmailRequest(
    @field:Email
    val email: String,
)
