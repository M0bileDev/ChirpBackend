package com.mobidev.studio.chirp.api.mappers

import com.mobidev.studio.chirp.api.dto.ChatParticipantDto
import com.mobidev.studio.chirp.domain.model.ChatParticipant

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePicture,
    )
}