package com.mobidev.studio.chirp.domain.event

import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.UserId

data class ChatParticipantsLeftEvent(
    val chatId: ChatId,
    val userId: UserId
)
