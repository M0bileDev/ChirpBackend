package com.mobidev.studio.chirp.api.ws

enum class IncomingWebSocketMessageType {
    NEW_MESSAGE
}

enum class OutgoingWebSocketMessageType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    CHAT_PARTICIPANTS_CHANGED,
    ERROR
}