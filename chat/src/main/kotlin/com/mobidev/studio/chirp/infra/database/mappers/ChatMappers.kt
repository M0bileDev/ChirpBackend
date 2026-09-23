package com.mobidev.studio.chirp.infra.database.mappers

import com.mobidev.studio.chirp.domain.model.ChatParticipant
import com.mobidev.studio.chirp.infra.database.entities.ChatParticipantEntity


fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePicture = profilePicture
    )
}