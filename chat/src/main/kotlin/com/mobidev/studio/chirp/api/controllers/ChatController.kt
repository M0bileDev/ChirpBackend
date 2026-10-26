package com.mobidev.studio.chirp.api.controllers

import com.mobidev.studio.chirp.api.dto.AddParticipantToChatDto
import com.mobidev.studio.chirp.api.dto.ChatDto
import com.mobidev.studio.chirp.api.dto.ChatMessageDto
import com.mobidev.studio.chirp.api.mappers.toChatDto
import com.mobidev.studio.chirp.api.request.CreateChatRequest
import com.mobidev.studio.chirp.api.util.requestUserId
import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

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

    @PostMapping("/{$CHAT_ID}/add")
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

    @DeleteMapping("/{$CHAT_ID}/leave")
    fun leaveChat(
        @PathVariable chatId: ChatId,
    ) {
        chatService.removeParticipantFromChat(
            chatId = chatId,
            userId = requestUserId
        )
    }

    @GetMapping("/{$CHAT_ID}/messages")
    fun getMessagesFromChat(
        @PathVariable(CHAT_ID) chatId: ChatId,
        @RequestParam("before", required = false) before: Instant? = null,
        @RequestParam("pageSize", required = false) pageSize: Int = DEFAULT_PAGE_SIZE,
    ): List<ChatMessageDto> {
        return chatService.getChatMessages(
            chatId = chatId,
            before = before,
            pageSize = pageSize
        )
    }

    @GetMapping("/{chatId}")
    fun getChatById(
        @PathVariable("chatId") chatId: ChatId,
    ): ChatDto {
        return chatService.getChatById(
            chatId = chatId,
            requestUserId = requestUserId
        )?.toChatDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping
    fun getChatsForUser(): List<ChatDto> {
        return chatService.findChatsByUserId(
            userId = requestUserId
        ).map {
            it.toChatDto()
        }
    }

    companion object {
        private const val CHAT_ID = "chatId"
        private const val DEFAULT_PAGE_SIZE = 20
    }
}