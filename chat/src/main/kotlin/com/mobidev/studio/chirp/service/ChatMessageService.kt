package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.infra.database.repositories.ChatMessageRepository
import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import com.mobidev.studio.chirp.infra.database.repositories.ChatRepository
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository
) {

}