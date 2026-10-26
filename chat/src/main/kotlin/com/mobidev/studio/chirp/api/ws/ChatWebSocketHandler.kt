package com.mobidev.studio.chirp.api.ws

import com.mobidev.studio.chirp.service.ChatMessageService
import com.mobidev.studio.chirp.service.ChatService
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

// manages active connections, properly receives messages
// and sends them into clients
@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
) {

}