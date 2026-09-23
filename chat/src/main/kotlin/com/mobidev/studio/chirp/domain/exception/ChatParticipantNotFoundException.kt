package com.mobidev.studio.chirp.domain.exception

import com.mobidev.studio.chirp.domain.type.UserId

class ChatParticipantNotFoundException(
    val userId: UserId
): RuntimeException(
    "Chat participant with $userId was not found "
)