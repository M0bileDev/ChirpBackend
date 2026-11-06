package com.mobidev.studio.chirp.api.controllers

import com.mobidev.studio.chirp.api.dto.DeviceTokenDto
import com.mobidev.studio.chirp.api.mappers.toDeviceTokenDto
import com.mobidev.studio.chirp.api.mappers.toPlatform
import com.mobidev.studio.chirp.api.request.RegisterDeviceRequest
import com.mobidev.studio.chirp.api.util.requestUserId
import com.mobidev.studio.chirp.service.PushNotificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notification")
class NotificationController(
    val pushNotificationService: PushNotificationService
) {

    @PostMapping("/register")
    fun registerDeviceToken(
        @Valid @RequestBody body: RegisterDeviceRequest
    ): DeviceTokenDto {
        return pushNotificationService.registerDevice(
            userId = requestUserId,
            token = body.token,
            platform = body.platformDto.toPlatform()
        ).toDeviceTokenDto()
    }
}