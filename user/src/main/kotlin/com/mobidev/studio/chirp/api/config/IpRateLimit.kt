package com.mobidev.studio.chirp.api.config

import java.util.concurrent.TimeUnit

// default values -> client can call 60 requests per 1 minute,
// 61st request will be blocked
annotation class IpRateLimit(
    val requests: Int = 60,
    val duration: Long = 1L,
    val unit: TimeUnit = TimeUnit.MINUTES,
)
