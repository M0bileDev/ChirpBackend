package com.mobidev.studio.chirp.api.controllers

import com.mobidev.studio.chirp.api.dto.AddParticipantToChatDto
import com.mobidev.studio.chirp.api.dto.ChatDto
import com.mobidev.studio.chirp.api.mappers.toChatDto
import com.mobidev.studio.chirp.api.request.CreateChatRequest
import com.mobidev.studio.chirp.api.util.requestUserId
import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

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

    @PostMapping("/{chatId}/add")
    fun addChatParticipants(
        @PathVariable chatId: ChatId,
        @Valid @RequestBody body: AddParticipantToChatDto
    ): ChatDto {
        return chatService.addParticipantsToChat(
            requestUserId = requestUserId,
            chatId = chatId,
            userIds = body.userIds.toSet()
        ).toChatDto()
    }

    @DeleteMapping("/{chatId}/leave")
    fun leaveChat(
        @PathVariable chatId: ChatId,
    ) {
        chatService.removeParticipantFromChat(
            chatId = chatId,
            userId = requestUserId
        )
    }
}