package com.mobidev.studio.chirp.infra.service

import com.mobidev.studio.chirp.domain.exception.ChatParticipantNotFoundException
import com.mobidev.studio.chirp.domain.exception.InvalidChatSizeException
import com.mobidev.studio.chirp.domain.model.Chat
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.entities.ChatEntity
import com.mobidev.studio.chirp.infra.database.mappers.toChat
import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import com.mobidev.studio.chirp.infra.database.repositories.ChatRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository
) {

    fun createChat(
        creatorId: UserId,
        otherUserId: Set<UserId>
    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserId
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
}