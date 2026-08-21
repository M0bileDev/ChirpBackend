package com.mobidev.studio.chirp.infra.rate_limiting

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class EmailRateLimiter(
    // Serializes values as normal Strings (String value)
    private val redisTemplate: StringRedisTemplate
){

    companion object{
        // lock -> prevents sending emails when it exists
        private const val EMAIL_RATE_LIMIT_PREFIX = "rate_limit:email"
        // key for value that keeps information about how often reset email
        // verification endpoint was called
        private const val EMAIL_ATTEMPT_COUNT_PREFIX = "email_attempt_count"
    }
}