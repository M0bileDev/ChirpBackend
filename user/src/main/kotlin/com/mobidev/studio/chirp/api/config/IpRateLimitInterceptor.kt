package com.mobidev.studio.chirp.api.config

import com.mobidev.studio.chirp.infra.ip_resolver.IpResolver
import com.mobidev.studio.chirp.infra.rate_limiting.IpRateLimiter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class IpRateLimitInterceptor(
    private val ipRateLimiter: IpRateLimiter,
    private val ipResolver: IpResolver,
    @param:Value("\${chirp.rate-limit.ip.apply-limit}")
    private val applyLimit: Boolean,
) : HandlerInterceptor {
}