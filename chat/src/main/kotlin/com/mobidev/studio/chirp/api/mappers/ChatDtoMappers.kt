package com.mobidev.studio.chirp.api.mappers

import com.mobidev.studio.chirp.api.dto.ChatDto
import com.mobidev.studio.chirp.api.dto.ChatMessageDto
import com.mobidev.studio.chirp.api.dto.ChatParticipantDto
import com.mobidev.studio.chirp.domain.model.Chat
import com.mobidev.studio.chirp.domain.model.ChatMessage
import com.mobidev.studio.chirp.domain.model.ChatParticipant

fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map { it.toChatParticipantDto() },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        creator = creator.toChatParticipantDto()
    )
}

fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = createdAt,
        senderId = sender.userId
    )
}

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePicture,
    )
}