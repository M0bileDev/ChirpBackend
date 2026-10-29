package com.mobidev.studio.chirp.api.dto

import com.mobidev.studio.chirp.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId
)
