package com.mobidev.studio.chirp.api.exception_handling

import com.mobidev.studio.chirp.domain.exception.UserAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

// how to handle exceptions in RestController
@RestControllerAdvice
class AuthExceptionHandler {

    // returns http status code CONFLICT
    @ExceptionHandler(UserAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onUserAlreadyExists(
        e: UserAlreadyExistsException
    ) =
        // JSON result of user already exists exception
        mapOf(
        "code" to "USER_ALREADY_EXIST",
        "message" to e.message
    )
}