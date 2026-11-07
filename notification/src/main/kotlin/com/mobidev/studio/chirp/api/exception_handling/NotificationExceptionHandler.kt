package com.mobidev.studio.chirp.api.exception_handling

import com.mobidev.studio.chirp.domain.exception.InvalidDeviceTokenException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class NotificationExceptionHandler {

    @ExceptionHandler(InvalidDeviceTokenException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidDeviceToken(
        e: InvalidDeviceTokenException
    ) =
        // JSON result of user already exists exception
        mapOf(
            "code" to "INVALID_DEVICE_TOKEN",
            "message" to e.message
        )
}