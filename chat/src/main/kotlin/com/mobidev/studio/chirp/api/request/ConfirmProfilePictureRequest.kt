package com.mobidev.studio.chirp.api.request

/**
 * Client request send to the backend to confirm that picture has been uploaded
 */
data class ConfirmProfilePictureRequest(
    val publicUrl: String,
)
