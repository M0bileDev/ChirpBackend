package com.mobidev.studio.chirp.api.mappers

import com.mobidev.studio.chirp.api.dto.PictureUploadDto
import com.mobidev.studio.chirp.domain.model.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toDto(): PictureUploadDto {
    return PictureUploadDto(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers,
        expiresAt = expiresAt,
    )
}