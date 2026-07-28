package com.mobidev.studio.chirp

import com.mobidev.studio.chirp.infra.database.entities.UserEntity
import com.mobidev.studio.chirp.infra.database.repositories.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class ChirpApplication

fun main(args: Array<String>) {
	runApplication<ChirpApplication>(*args)
}

@Component
class Demo(
	private val repository: UserRepository
){
	// execute this method once all backend configuration is done
	@PostConstruct
	fun init(){
		repository.save(UserEntity(
			email = "sample@test.com",
			username = "sample",
			password = "sample"
		))
	}
}