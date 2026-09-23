package com.mobidev.studio.chirp.api.controllers

import com.mobidev.studio.chirp.api.dto.ChatDto
import com.mobidev.studio.chirp.api.mappers.toChatDto
import com.mobidev.studio.chirp.api.request.CreateChatRequest
import com.mobidev.studio.chirp.api.util.requestUserId
import com.mobidev.studio.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/chat")
class ChatController(
    private val chatService: ChatService,
) {

    @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }
}