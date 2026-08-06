package com.mobidev.studio.chirp.service.auth

import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.io.encoding.Base64

@Service
class JwtService(
    // values gained from application.yaml file
    @param:Value("\$jwt.secret}") private val secretBase64: String,
    @param:Value("\${jwt.expiration-minutes}") private val expirationMinutes: Int,
) {

    private val secretKey = Keys.hmacShaKeyFor(
        Base64.decode(secretBase64)
    )
    private val accessTokenValidityMs = expirationMinutes * 60 * 1000
    val refreshTokenValidityMs = 30 * 24 * 60 * 60 * 1000
}