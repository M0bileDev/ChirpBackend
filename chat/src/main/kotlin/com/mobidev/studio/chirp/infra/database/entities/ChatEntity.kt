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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "creator_id",
        nullable = false
    )
    var creator: ChatParticipantEntity,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "chat_participants_cross_ref",
        joinColumns = [JoinColumn(name = "chat_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")],
        indexes = [
            Index(
                name = "idx_chat_participant_chat_id_user_id",
//                order matters
//                who is in chat x?
                columnList = "chat_id, user_id",
                unique = true
            ),
            Index(
                name = "idx_chat_participant_user_id_chat_id",
//                order matters
//                what chats is user x in?
                columnList = "user_id, chat_id",
                unique = true
            ),
        ]
    )
    var participants: Set<ChatParticipantEntity> = emptySet(),
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)