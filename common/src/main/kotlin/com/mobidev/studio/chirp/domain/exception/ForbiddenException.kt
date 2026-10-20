package com.mobidev.studio.chirp.domain.exception

class ForbiddenException : RuntimeException(
    "You don't have permission to perform this operation"
)