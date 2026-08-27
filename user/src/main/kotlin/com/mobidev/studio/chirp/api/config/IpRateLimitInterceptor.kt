package com.mobidev.studio.chirp.api.config

import com.mobidev.studio.chirp.domain.exception.RateLimitException
import com.mobidev.studio.chirp.infra.ip_resolver.IpResolver
import com.mobidev.studio.chirp.infra.rate_limiting.IpRateLimiter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

@Component
class IpRateLimitInterceptor(
    private val ipRateLimiter: IpRateLimiter,
    private val ipResolver: IpResolver,
    @param:Value("\${chirp.rate-limit.ip.apply-limit}")
    private val applyLimit: Boolean,
) : HandlerInterceptor {

    // intercept request
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any?
    ): Boolean {
        // check if the root function is annotated with custom annotation of @IpRateLimit and if so apply ip rate limit

        // HandlerMethod has information about annotations from controller
        if(handler is HandlerMethod && applyLimit) {
            val annotation = handler.getMethodAnnotation(IpRateLimit::class.java)

            if(annotation != null) {
                val clientIp = ipResolver.getClientIp(request = request)

                return try {
                    ipRateLimiter.withIpRateLimit(
                        ipAddress = clientIp,
                        resetsIn = Duration.of(
                            annotation.duration,
                            annotation.unit.toChronoUnit()
                        ),
                        maxRequestPerIp = annotation.requests,
                        action = { true }
                    )
                }catch (_: RateLimitException){
                    // rate limit has been exceeded
                    response.sendError(429)
                    // do not continue from this interceptor
                    false
                }
            }
        }

        // in case handler is not HandlerMethod let it goes
        return true
    }
}