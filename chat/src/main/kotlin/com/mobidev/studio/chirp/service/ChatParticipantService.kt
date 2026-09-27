package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.domain.model.ChatParticipant
import com.mobidev.studio.chirp.infra.database.mappers.toChatParticipantEntity
import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository
) {

    fun createChatParticipant(chatParticipant: ChatParticipant) {
        chatParticipantRepository.save(
            chatParticipant.toChatParticipantEntity()
        )
    }
}