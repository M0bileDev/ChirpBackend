package com.mobidev.studio.chirp.api.util

import com.mobidev.studio.chirp.domain.exception.UnauthorizedException
import com.mobidev.studio.chirp.domain.model.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId : UserId
    // principal -> user id from the token that was attached in request
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        // tried to access the request user id which should only be in authenticated endpoint,
        // but there was issue with extracting this information
        ?: throw UnauthorizedException()