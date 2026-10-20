package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.domain.exception.ChatNotFoundException
import com.mobidev.studio.chirp.domain.exception.ChatParticipantNotFoundException
import com.mobidev.studio.chirp.domain.exception.ForbiddenException
import com.mobidev.studio.chirp.domain.exception.InvalidChatSizeException
import com.mobidev.studio.chirp.domain.model.Chat
import com.mobidev.studio.chirp.domain.model.ChatMessage
import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.entities.ChatEntity
import com.mobidev.studio.chirp.infra.database.mappers.toChat
import com.mobidev.studio.chirp.infra.database.mappers.toChatMessage
import com.mobidev.studio.chirp.infra.database.repositories.ChatMessageRepository
import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import com.mobidev.studio.chirp.infra.database.repositories.ChatRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
) {

    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>
    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds
        )

        val allParticipants = otherParticipants.plus(creatorId)
        if (allParticipants.size < 2) throw InvalidChatSizeException()

        val creatorParticipant =
            chatParticipantRepository.findByIdOrNull(creatorId)
                ?: throw ChatParticipantNotFoundException(creatorId)

        val entity = ChatEntity(
            creator = creatorParticipant,
            participants = otherParticipants.plus(creatorParticipant),
        )
        return chatRepository.save(entity).toChat()
    }

    @Transactional
    fun addParticipantsToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId) ?: throw ChatNotFoundException()

        //user that wants to add other users must be present in this chat (as a host)
        val isRequestingUserInChat = chat.participants.any {
            it.userId == requestUserId
        }
        if (!isRequestingUserInChat) throw ForbiddenException()

        //check if all userIds are valid
        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = lastMessageFromChat(chatId)
        //upsert chat
        val updatedChat = chat.apply {
            this.participants = chat.participants.plus(users)
        }

        return chatRepository
            .save(
                updatedChat
            ).toChat(lastMessage)
    }

    private fun lastMessageFromChat(chatId: ChatId): ChatMessage? {
        val selectedChatId = setOf(chatId)

        return chatMessageRepository
            .findLatestMessagesByChatIds(selectedChatId)
            .firstOrNull()
            ?.toChatMessage()
    }
}