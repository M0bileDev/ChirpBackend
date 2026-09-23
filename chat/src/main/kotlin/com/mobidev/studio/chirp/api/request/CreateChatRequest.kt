package com.mobidev.studio.chirp.api.request

import com.mobidev.studio.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequest(
    @field:Size(
        // min = 1, because the argument is collection indexed from 0
        min = 1,
        message = "Chat must have at least 2 unique participants",
    )
    val otherUserIds: List<UserId>
)
