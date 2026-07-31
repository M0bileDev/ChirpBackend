package com.mobidev.studio.chirp.domain.exception

class UserAlreadyExistsException : RuntimeException(
    "A user with this username or email already exists."
)