package com.mobidev.studio.chirp.infra.push_notification

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
class FirebasePushNotificationService(
    @param:Value("\${firebase.credentials-path}") private val firebaseCredentialsPath: String,
    // tool used for loading json content from credentials path
    private val resourceLoader: ResourceLoader
) {
}