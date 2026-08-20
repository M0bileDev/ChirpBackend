package com.mobidev.studio.chirp.api.util

import jakarta.validation.Constraint
import jakarta.validation.constraints.Pattern
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
// leave empty, needed only for SpringBoot validation
@Constraint(validatedBy = [])
@Pattern(
    regexp = "^(?=.*[\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(.{8,})$",
    message = "Password must be at least 8 characters and contain at least one digit or special character"
)
annotation class Password(
    val message: String = "Password must be at least 8 characters and contain at least one digit or special character",
    // leave empty, needed only for SpringBoot validation
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
    )
