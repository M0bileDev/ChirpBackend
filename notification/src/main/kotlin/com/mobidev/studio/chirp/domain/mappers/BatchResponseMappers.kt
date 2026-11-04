package com.mobidev.studio.chirp.domain.mappers

import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.MessagingErrorCode
import com.mobidev.studio.chirp.domain.model.DeviceToken
import com.mobidev.studio.chirp.domain.model.PushNotificationSendResult
import org.slf4j.Logger

fun BatchResponse.toSendResult(
    allDeviceTokens: List<DeviceToken>,
    logger: Logger?
): PushNotificationSendResult {

    val succeeded = mutableListOf<DeviceToken>()
    val temporaryFailures = mutableListOf<DeviceToken>()
    val permanentFailures = mutableListOf<DeviceToken>()

    responses.forEachIndexed { index, response ->
        val deviceToken = allDeviceTokens[index]

        if (response.isSuccessful) {
            succeeded.add(deviceToken)
        } else {
            val errorCode = response.exception?.messagingErrorCode
            logger?.warn("Failed to send notification to device token ${deviceToken.token}, errorCode: $errorCode")

            when (errorCode) {
                null,
                MessagingErrorCode.INTERNAL,
                MessagingErrorCode.QUOTA_EXCEEDED,
                MessagingErrorCode.UNAVAILABLE -> {
                    temporaryFailures.add(deviceToken)
                }

                MessagingErrorCode.UNREGISTERED,
                MessagingErrorCode.SENDER_ID_MISMATCH,
                MessagingErrorCode.INVALID_ARGUMENT,
                MessagingErrorCode.THIRD_PARTY_AUTH_ERROR -> {
                    permanentFailures.add(deviceToken)
                }
            }
        }
    }

    logger?.debug(
        "Push notifications sent. " +
                "Succeeded: ${succeeded.size}, " +
                "temporary failures: ${temporaryFailures.size}, " +
                "permanent failures: ${permanentFailures.size}"
    )

    return PushNotificationSendResult(
        succeeded = succeeded.toList(),
        temporaryFailures = temporaryFailures.toList(),
        permanentFailures = permanentFailures.toList(),
    )
}