package com.mobidev.studio.chirp.domain.exception

class UserNotFoundException: RuntimeException(
    "User not found",
)