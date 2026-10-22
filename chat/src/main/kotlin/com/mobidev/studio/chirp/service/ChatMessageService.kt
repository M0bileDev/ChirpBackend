package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.api.dto.ChatMessageDto
import com.mobidev.studio.chirp.api.mappers.toChatMessageDto
import com.mobidev.studio.chirp.domain.exception.ChatNotFoundException
import com.mobidev.studio.chirp.domain.exception.ChatParticipantNotFoundException
import com.mobidev.studio.chirp.domain.exception.ForbiddenException
import com.mobidev.studio.chirp.domain.exception.MessageNotFoundException
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

    @Transactional
    fun deleteMessage(
        messageId: ChatId,
        requestUserId: UserId
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException()

        if (message.sender.userId != requestUserId) throw ForbiddenException()

        chatMessageRepository.delete(message)
    }
}