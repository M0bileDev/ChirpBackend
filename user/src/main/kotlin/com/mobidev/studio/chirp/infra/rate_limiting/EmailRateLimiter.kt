package com.mobidev.studio.chirp.infra.rate_limiting

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component

@Component
class EmailRateLimiter(
    // Serializes values as normal Strings (String value)
    private val redisTemplate: StringRedisTemplate
) {

    companion object {
        // lock -> prevents sending emails when it exists
        private const val EMAIL_RATE_LIMIT_PREFIX = "rate_limit:email"

        // key for value that keeps information about how often reset email
        // verification endpoint was called
        private const val EMAIL_ATTEMPT_COUNT_PREFIX = "email_attempt_count"
    }

    //    wire resource, after packaging all files in jar, and will be visible as classpath
    @Value("classpath:email_rate_limit.lua")
    lateinit var rateLimitResource: Resource

    private val rateLimitScript by lazy {
        val script = rateLimitResource.inputStream.use {
            it.readBytes().decodeToString()
        }
        @Suppress("UNCHECKED_CAST")
        DefaultRedisScript(script, List::class.java as Class<List<Long>>)
    }
}