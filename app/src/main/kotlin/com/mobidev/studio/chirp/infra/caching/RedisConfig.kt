package com.mobidev.studio.chirp.infra.caching

import com.mobidev.studio.chirp.domain.events.ChirpEvent
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.kotlinModule
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig {

    @Bean
    fun cacheManager(
        connectionFactory: LettuceConnectionFactory
    ): RedisCacheManager {
        // support serialization of subclasses
        val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(ChirpEvent::class.java)
            // allow Java lists
            .allowIfSubType("java.util.")
            // Kotlin collections
            .allowIfSubType("kotlin.collections.")
            .allowIfSubType("com.mobidev.studio.chirp")
            .build()

        val objectMapper = JsonMapper.builder()
            .addModule(kotlinModule())
            .polymorphicTypeValidator(polymorphicTypeValidator)
            .activateDefaultTyping(polymorphicTypeValidator, DefaultTyping.NON_FINAL)
            .build()

        val cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(MESSAGES_ENTRY_TTL_HOUR))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJacksonJsonRedisSerializer(objectMapper)
                )
            )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(cacheConfig)
            .withCacheConfiguration(
                MESSAGES_CACHE_NAME,
                cacheConfig.entryTtl(Duration.ofMinutes(CACHING_MESSAGES_TTL_MINUTES))
            )
            // cache will only be populated if the underlying transaction (database transaction) was successful
            .transactionAware()
            .build()
    }

    companion object {
        private const val MESSAGES_ENTRY_TTL_HOUR = 1L
        private const val CACHING_MESSAGES_TTL_MINUTES = 30L
        private const val MESSAGES_CACHE_NAME = "messages"
    }
}