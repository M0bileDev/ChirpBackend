package com.mobidev.studio.chirp.infra.database.repositories

import com.mobidev.studio.chirp.infra.database.entities.EmailVerificationTokenEntity
import com.mobidev.studio.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationTokenEntity, Long> {
    fun findByToken(token: String): EmailVerificationTokenEntity?
    fun deleteByExpiresAtLessThan(now: Instant)

    // custom query
    @Modifying
    @Query("""
        UPDATE EmailVerificationTokenEntity entity
        SET entity.usedAt = CURRENT_TIMESTAMP
        WHERE entity.user = :user
    """)
    fun invalidateActiveTokensForUser(user: UserEntity)
}