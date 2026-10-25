package com.mobidev.studio.chirp.domain.events.chat

import com.mobidev.studio.chirp.domain.events.ChirpEvent
import java.time.Instant
import java.util.*

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
) : ChirpEvent