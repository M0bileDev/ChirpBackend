package com.mobidev.studio.chirp.infra.database.entities

import com.mobidev.studio.chirp.domain.model.UserId
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
@Table(
    name = "users",
    schema = "user_service",
    indexes = [
        Index(name = "idx_users_email", columnList = "email"),
        Index(name = "idx_users_username", columnList = "username"),
    ]
)
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UserId? = null,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = false, unique = true)
    var username: String,
    @Column(nullable = false)
    var password: String,
    @Column(nullable = false)
    var hasVerifiedEmail: Boolean = false,
    @CreationTimestamp
    var createdAt: Instant = Clock.System.now(),
    @UpdateTimestamp
    var updatedAt: Instant = Clock.System.now(),
)