package com.mobidev.studio.user.infra.database.repositories

import com.mobidev.studio.user.domain.model.UserId
import com.mobidev.studio.user.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, UserId> {
}