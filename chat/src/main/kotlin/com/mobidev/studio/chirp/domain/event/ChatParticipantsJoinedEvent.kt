package com.mobidev.studio.chirp.domain.event

import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)
