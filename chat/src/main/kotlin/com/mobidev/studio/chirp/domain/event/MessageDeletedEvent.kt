package com.mobidev.studio.chirp.domain.event

import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId,
)