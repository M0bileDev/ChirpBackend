package com.mobidev.studio.chirp.service.auth

import com.mobidev.studio.chirp.domain.exception.InvalidTokenException
import com.mobidev.studio.chirp.domain.model.UserId
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
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
    private val accessTokenValidityMs = expirationMinutes * 60 * 1000L
    val refreshTokenValidityMs = 30 * 24 * 60 * 60 * 1000L

    private fun generateToken(userId: UserId, type: String, expiry: Long): String {
        val now = Date()
        val expirationDate = Date(now.time + expiry)

        return Jwts
            .builder()
            .subject(userId.toString())
            .claim(TYPE, type)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith(BEARER_PREFIX)) token.removePrefix(BEARER_PREFIX) else token
        val jwtsParser = Jwts.parser()
            .verifyWith(secretKey)
            .build()

        return try {
            jwtsParser.parseSignedClaims(rawToken).payload
        } catch (_: Exception) {
            null
        }
    }

    fun generateAccessToken(userId: UserId): String {
        return generateToken(userId = userId, type = ACCESS, expiry = accessTokenValidityMs)
    }

    fun generateRefreshToken(userId: UserId): String {
        return generateToken(userId = userId, type = REFRESH, expiry = refreshTokenValidityMs)
    }

    fun validateAccessToken(accessToken: String): Boolean {
        val claims = parseAllClaims(token = accessToken) ?: return false
        val tokenType = claims[TYPE] as? String ?: return false

        return tokenType == ACCESS
    }

    fun validateRefreshToken(refreshToken: String): Boolean {
        val claims = parseAllClaims(token = refreshToken) ?: return false
        val tokenType = claims[TYPE] as? String ?: return false

        return tokenType == REFRESH
    }

    fun getUserIdFromToken(token: String): UserId {
        val claims = parseAllClaims(token) ?: throw InvalidTokenException("Attached JWT token is invalid.")

        return UUID.fromString(claims.subject)
    }

    companion object {
        private const val ACCESS = "access"
        private const val REFRESH = "refresh"
        private const val TYPE = "type"
        private const val BEARER_PREFIX = "Bearer "
    }
}