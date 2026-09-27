package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository
)