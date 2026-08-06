package com.mobidev.studio.chirp.service.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class JwtService(
    // values gained from application.yaml file
    @param:Value("\$jwt.secret}") private val secret: String,
    @param:Value("\${jwt.expiration-minutes}") private val expirationMinutes: Int,
) {
}