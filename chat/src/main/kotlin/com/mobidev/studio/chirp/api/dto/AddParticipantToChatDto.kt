package com.mobidev.studio.chirp.api.dto

import com.mobidev.studio.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

data class AddParticipantToChatDto(
    @field:Size(min = 1)
    val userIds: Set<UserId>,
)