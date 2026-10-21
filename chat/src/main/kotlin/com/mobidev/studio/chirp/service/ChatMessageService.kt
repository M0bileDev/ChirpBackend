package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.api.dto.ChatMessageDto
import com.mobidev.studio.chirp.api.mappers.toChatMessageDto
import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.infra.database.mappers.toChatMessage
import com.mobidev.studio.chirp.infra.database.repositories.ChatMessageRepository
import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import com.mobidev.studio.chirp.infra.database.repositories.ChatRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository
) {

    // violation of "separation of concerns" because return type is dto, because domain model
    // contains "sender" which is not needed for all the users
    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int
    ): List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            )
            .content
            // result has to be reversed, sorted by createdAt field in SQL query
            .asReversed()
            .map {
                it.toChatMessage().toChatMessageDto()
            }
    }
}