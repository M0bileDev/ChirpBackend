package com.mobidev.studio.chirp.domain.exception

class RateLimitException(
    //number of seconds user can retry call
    val resetsInSeconds: Long,
) : RuntimeException(
    "Rate limit exceeded, please retry again in $resetsInSeconds seconds."
)