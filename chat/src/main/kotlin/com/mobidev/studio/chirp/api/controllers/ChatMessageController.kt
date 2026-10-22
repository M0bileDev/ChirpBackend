package com.mobidev.studio.chirp.api.controllers

import com.mobidev.studio.chirp.service.ChatMessageService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/messages")
class ChatMessageController(
    private val chatMessageService: ChatMessageService
) {
}