package com.mobidev.studio.chirp.infra.database.repositories

import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.entities.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRepository : JpaRepository<ChatEntity, ChatId> {

//    Find a chat (no matter if it contains participants and creator LEFT JOIN)
//    (simplified thanks to the jpa declarations @JoinColumn inside ChatEntity)
//    and contains at least 1 participant with correct user id
    @Query(
        """
            SELECT c 
            FROM ChatEntity c
            LEFT JOIN FETCH c.participants
            LEFT JOIN FETCH c.creator
            WHERE c.id = :id
            AND EXISTS (
            SELECT 1
            FROM c.participants p
            WHERE p.userId = :userId
            )
        """
    )
    fun findChatById(id: ChatId, userId: UserId): ChatEntity?
}