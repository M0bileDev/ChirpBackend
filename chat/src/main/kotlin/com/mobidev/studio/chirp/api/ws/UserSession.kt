package com.mobidev.studio.chirp.api.ws

import com.mobidev.studio.chirp.domain.type.UserId
import org.springframework.web.socket.WebSocketSession

data class UserSession(
    val userId: UserId,
    val session: WebSocketSession
)
