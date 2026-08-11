package com.mobidev.studio.chirp.api.exception_handling

import com.mobidev.studio.chirp.domain.exception.InvalidTokenException
import com.mobidev.studio.chirp.domain.exception.UserAlreadyExistsException
import com.mobidev.studio.chirp.domain.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

// global configuration how to handle exceptions in RestController
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

    @ExceptionHandler(InvalidTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidToken(
        e: InvalidTokenException
    ) =
        mapOf(
            "code" to "INVALID_TOKEN",
            "message" to e.message
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(
        e: MethodArgumentNotValidException,
    ): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.allErrors.map {
            it.defaultMessage ?: "Invalid value"
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "code" to "VALIDATION_ERROR",
                    "errors" to errors
                )
            )
    }

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onUserNotFound(
        e: UserNotFoundException
    ) = mapOf(
            "code" to "USER_NOT_FOUND",
            "message" to e.message
        )
}