package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.api.dto.ChatMessageDto
import com.mobidev.studio.chirp.api.mappers.toChatMessageDto
import com.mobidev.studio.chirp.domain.event.ChatParticipantLeftEvent
import com.mobidev.studio.chirp.domain.event.ChatParticipantsJoinedEvent
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
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
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
        // validate if chat exists
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

        val chatParticipantsJoinedEvent = ChatParticipantsJoinedEvent(
            chatId = chatId,
            userIds = userIds
        )
        applicationEventPublisher.publishEvent(
            chatParticipantsJoinedEvent
        )

        return chatRepository
            .save(
                updatedChat
            ).toChat(lastMessage)
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId
    ) {
        // validate if chat exists
        val chat = chatRepository.findByIdOrNull(chatId) ?: throw ChatNotFoundException()

        //validate if participant exists in this chat
        val participant =
            chat.participants.find { it.userId == userId } ?: throw ChatParticipantNotFoundException(userId)

        //check if the last participant leaves the chat -> remove chat from db
        val newParticipantsSize = chat.participants.size - 1
        if (newParticipantsSize == 0) {
            chatRepository.deleteById(chatId)
            return
        }

        val updatedChat = chat.apply {
            this.participants = chat.participants.minus(participant)
        }
        chatRepository.save(updatedChat)

        val chatParticipantLeftEvent = ChatParticipantLeftEvent(
            chatId = chatId,
            userId = userId
        )
        applicationEventPublisher.publishEvent(
            chatParticipantLeftEvent
        )
    }

    private fun lastMessageFromChat(chatId: ChatId): ChatMessage? {
        val selectedChatId = setOf(chatId)

        return chatMessageRepository
            .findLatestMessagesByChatIds(selectedChatId)
            .firstOrNull()
            ?.toChatMessage()
    }

    // violation of "separation of concerns" because return type is dto, because domain model
    // contains "sender" which is not needed for all the users
    @Cacheable(
        value = [MESSAGES_CACHE_NAME],
        key = "#chatId",
        // by default redis will cache the most recent
        // list of messages, it could be at first page 1, then page 2
        // etc. so a client expecting to get page 1 will get page 2;
        // rule below, first page (before is null) and client
        // cant manipulate page size, so no more than 50
        condition = "#before == null && #pageSize <= 50",
        // two simultaneous requests, will be handled in order
        sync = true
    )
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

    fun getChatById(
        chatId: ChatId,
        requestUserId: UserId
    ): Chat? {
        val lastMessageFromChat = lastMessageFromChat(chatId)

        return chatRepository.findChatById(
            chatId,
            requestUserId
        )?.toChat(
            lastMessageFromChat
        )
    }

    fun findChatsByUserId(userId: UserId): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId)
        val chatIds = chatEntities.mapNotNull { it.chatId }
        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatIds(chatIds.toSet())
            .associateBy { it.chatId }

        return chatEntities
            .map {
                it.toChat(
                    lastMessage = latestMessages[it.chatId]?.toChatMessage()
                )
            }
            .sortedByDescending {
                it.lastActivityAt
            }
    }

    companion object {
        const val MESSAGES_CACHE_NAME = "messages"
    }
}