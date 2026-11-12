package com.mobidev.studio.chirp.service.util

import com.mobidev.studio.chirp.domain.type.ChatId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component

@Component
class MessageCacheEvictionHelper {

    // separate this function to different class, because Spring won't
    // execute this otherwise
    @CacheEvict(
        value = [MESSAGES_CACHE_NAME],
        key = "#chatId"
    )
    fun evictMessagesCache(chatId: ChatId) {
        // NO-OP: Let spring framework handle the cache evict
    }

    companion object {
        const val MESSAGES_CACHE_NAME = "messages"
    }
}