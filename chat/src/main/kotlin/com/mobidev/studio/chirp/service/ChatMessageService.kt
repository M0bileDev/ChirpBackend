package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.api.dto.ChatMessageDto
import com.mobidev.studio.chirp.api.mappers.toChatMessageDto
import com.mobidev.studio.chirp.domain.exception.ChatNotFoundException
import com.mobidev.studio.chirp.domain.exception.ChatParticipantNotFoundException
import com.mobidev.studio.chirp.domain.model.ChatMessage
import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.entities.ChatMessageEntity
import com.mobidev.studio.chirp.infra.database.mappers.toChatMessage
import com.mobidev.studio.chirp.infra.database.repositories.ChatMessageRepository
import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import com.mobidev.studio.chirp.infra.database.repositories.ChatRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
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

    @Transactional
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        // sender creates the messageId, the id will be sent over web socket, sender will receive the message back from the server
        // if it was sent successfully (there has to be a mechanism that tells message was send/received successfully or not),
        // messageId will be compared by the client
        messageId: ChatId
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId) ?: throw ChatNotFoundException()
        val sender =
            chatParticipantRepository.findByIdOrNull(senderId) ?: throw ChatParticipantNotFoundException(senderId)

        val newMessage = ChatMessageEntity(
            id = messageId,
            content = content.trim(),
            chatId = chatId,
            chat = chat,
            sender = sender
        )
        val savedMessage = chatMessageRepository.save(
            newMessage
        )

        return savedMessage.toChatMessage()
    }
}