package com.mobidev.studio.chirp.infra.database.repositories

import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.ChatMessageId
import com.mobidev.studio.chirp.infra.database.entities.ChatMessageEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface ChatMessageRepository : JpaRepository<ChatMessageEntity, ChatMessageId> {

//    Using Slice instead of Page because this doesn't care about previous/next page and overall page count
    @Query(
        """
            SELECT m
            FROM ChatMessageEntity m
            WHERE m.chatId = :chatId
            AND m.createdAt < :before
            ORDER BY m.createdAt DESC
        """
    )
    fun findByChatIdBefore(
        chatId: ChatId,
        before: Instant,
        pageable: Pageable
    ): Slice<ChatMessageEntity>

//    subquery executing for each message row
    @Query("""
        SELECT m
        FROM ChatMessageEntity m
        LEFT JOIN m.sender
        WHERE m.chatId in :chatIds
        AND (m.createdAt, m.id) = (
            SELECT m2.createdAt, m2.id
            FROM ChatMessageEntity m2
            WHERE m2.chatId = m.chatId
            ORDER BY m2.createdAt DESC
            LIMIT 1
        )
    """)
    fun findLatestMessagesByChatIds(
        chatIds: Set<ChatId>,
    ): List<ChatMessageEntity>
}