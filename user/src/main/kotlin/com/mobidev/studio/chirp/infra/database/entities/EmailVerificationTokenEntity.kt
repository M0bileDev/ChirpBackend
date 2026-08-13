package com.mobidev.studio.chirp.infra.database.entities

import com.mobidev.studio.chirp.infra.security.TokenGenerator
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "email_verification_tokens",
    schema = "user_service",
)
class EmailVerificationTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false, unique = true)
    var token: String = TokenGenerator.generateSecureToken(),
    @Column(nullable = false)
    var expiresAt: Instant,
    // many email verification tokens belong to one user
    // but, join operation will be done only when usage of the user will be done
    // until, then no join operation is made (lazy)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @Column
    var usedAt: Instant? = null,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
){
    val isUsed: Boolean
        get() = usedAt != null
}