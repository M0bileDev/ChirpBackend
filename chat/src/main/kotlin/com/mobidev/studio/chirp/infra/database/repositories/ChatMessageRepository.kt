package com.mobidev.studio.chirp.infra.database.repositories

import com.mobidev.studio.chirp.domain.type.ChatMessageId
import com.mobidev.studio.chirp.infra.database.entities.ChatMessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessageEntity, ChatMessageId> {
}