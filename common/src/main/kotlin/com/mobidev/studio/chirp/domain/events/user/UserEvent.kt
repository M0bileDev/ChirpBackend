package com.mobidev.studio.chirp.domain.events.user

import com.mobidev.studio.chirp.domain.events.ChirpEvent
import java.time.Instant
import java.util.*

sealed class UserEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = UserEventConstants.USER_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
) : ChirpEvent{

}