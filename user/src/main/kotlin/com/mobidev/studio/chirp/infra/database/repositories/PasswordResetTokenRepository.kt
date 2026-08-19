package com.mobidev.studio.chirp.infra.database.repositories

import com.mobidev.studio.chirp.infra.database.entities.EmailVerificationTokenEntity
import com.mobidev.studio.chirp.infra.database.entities.PasswordResetTokenEntity
import com.mobidev.studio.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface PasswordResetTokenRepository: JpaRepository<PasswordResetTokenEntity, Long> {
    fun findByToken(token: String): PasswordResetTokenEntity?
    fun deleteByExpiresAtLessThan(now: Instant)

    // custom query
    @Modifying
    @Query("""
        UPDATE PasswordResetTokenEntity entity
        SET entity.used_at = CURRENT_TIMESTAMP
        WHERE entity.user = :user
    """)
    fun invalidateActiveTokensForUser(user: UserEntity)
}