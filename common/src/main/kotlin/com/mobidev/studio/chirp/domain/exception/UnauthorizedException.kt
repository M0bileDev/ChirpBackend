package com.mobidev.studio.chirp.domain.exception

class UnauthorizedException : RuntimeException(
    "User is not authorized to perform this operation"
)