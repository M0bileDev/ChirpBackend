package com.mobidev.studio.chirp.infra.database.entities

import com.mobidev.studio.chirp.domain.type.ChatId
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "chats",
    schema = "chat_service",
)
class ChatEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var chatId: ChatId? = null,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)