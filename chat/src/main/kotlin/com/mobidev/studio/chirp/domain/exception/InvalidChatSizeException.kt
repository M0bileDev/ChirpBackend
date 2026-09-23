package com.mobidev.studio.chirp.domain.exception

class InvalidChatSizeException: RuntimeException(
    "There must be at least two unique participants in the same chat"
)