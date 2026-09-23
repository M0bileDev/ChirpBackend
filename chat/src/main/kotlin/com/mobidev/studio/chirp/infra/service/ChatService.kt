package com.mobidev.studio.chirp.infra.service

import com.mobidev.studio.chirp.infra.database.repositories.ChatRepository
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatRepository: ChatRepository
){

}