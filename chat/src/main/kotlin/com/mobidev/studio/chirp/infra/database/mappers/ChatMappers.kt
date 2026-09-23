package com.mobidev.studio.chirp.infra.database.mappers

import com.mobidev.studio.chirp.domain.model.Chat
import com.mobidev.studio.chirp.domain.model.ChatMessage
import com.mobidev.studio.chirp.domain.model.ChatParticipant
import com.mobidev.studio.chirp.infra.database.entities.ChatEntity
import com.mobidev.studio.chirp.infra.database.entities.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        id = chatId!!,
        participants = participants.map { it.toChatParticipant() }.toSet(),
        creator = creator.toChatParticipant(),
        lastMessage = lastMessage,
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        createdAt = createdAt,
    )
}

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePicture = profilePicture
    )
}