package com.mobidev.studio.chirp.infra.database.entities

import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.ChatMessageId
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant

@Entity
@Table(
    name = "chat_messages",
    schema = "chat_service",
    indexes = [
        Index(
            name = "idx_chat_message_chat_id_created_at",
            columnList = "chat_id, created_at DESC"
        )
    ]
)
class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: ChatMessageId? = null,
    @Column(nullable = false)
    var content: String,
//    @Column, not @JoinColumn - chatId is a basic value, and @JoinColumn is ignored on basic fields,
//    which made Hibernate derive a second logical name ("chatId") for the same physical "chat_id" column
    @Column(
        name = "chat_id",
        nullable = false,
        updatable = false
    )
    var chatId: ChatId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "chat_id",
        nullable = false,
        insertable = false,
        updatable = false
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    var chat: ChatEntity? = null,
//    Java-Kotlin interop -> event if set as not-nullable,
//    default value has to be provided for Hibernate to autopopulate this value,
//    because if not, then the moment ChatMessageEntity will be requested,
//    expects chat to be passed
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "sender_id",
        nullable = false,
        insertable = false,
        updatable = false
    )
//    nullable for the same reason as above
    var sender: ChatParticipantEntity,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)