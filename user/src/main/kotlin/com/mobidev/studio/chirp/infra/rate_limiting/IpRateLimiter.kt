package com.mobidev.studio.chirp.infra.rate_limiting

import com.mobidev.studio.chirp.domain.exception.RateLimitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import java.time.Duration

class IpRateLimiter(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val IP_RATE_LIMIT_PREFIX = "rate_limit:ip"
    }

    @Value("classpath:ip_rate_limit.lua")
    lateinit var rateLimitResource: Resource

    private val rateLimitScript by lazy {
        val script = rateLimitResource.inputStream.use {
            it.readBytes().decodeToString()
        }
        @Suppress("UNCHECKED_CAST")
        DefaultRedisScript(script, List::class.java as Class<List<Long>>)
    }

    fun <T> withIpRateLimit(
        // how many requests were made within this address
        ipAddress: String,
        // duration the values are going to be reset
        resetsIn: Duration,
        maxRequestPerIp: Int,
        action: () -> T
    ): T {
        val key = "$IP_RATE_LIMIT_PREFIX:$ipAddress"
        val result = redisTemplate.execute(
            rateLimitScript,
            listOf(key),
            maxRequestPerIp.toString(), resetsIn.toString()
        )
        val currentCount = result[0]

        return if (currentCount <= maxRequestPerIp) {
            action()
        } else {
            val ttl = result[1]
            throw RateLimitException(resetsInSeconds = ttl)
        }
    }
}