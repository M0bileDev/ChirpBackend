package com.mobidev.studio.chirp.domain.exception

class StorageException(
    override val message: String?
): RuntimeException(
    message ?: "Storage Failed"
)