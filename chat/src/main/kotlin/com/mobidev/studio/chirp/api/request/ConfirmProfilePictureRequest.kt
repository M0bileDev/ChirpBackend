package com.mobidev.studio.chirp.api.request

import jakarta.validation.constraints.NotBlank

/**
 * Client request send to the backend to confirm that picture has been uploaded
 */
data class ConfirmProfilePictureRequest(
    @field:NotBlank
    val publicUrl: String,
)
