package com.mobidev.studio.chirp.infra.database.repositories

import com.mobidev.studio.chirp.domain.model.UserId
import com.mobidev.studio.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?
}