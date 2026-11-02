package com.mobidev.studio.chirp.api.controllers

import com.mobidev.studio.chirp.api.dto.ChatParticipantDto
import com.mobidev.studio.chirp.api.dto.PictureUploadDto
import com.mobidev.studio.chirp.api.mappers.toChatParticipantDto
import com.mobidev.studio.chirp.api.mappers.toDto
import com.mobidev.studio.chirp.api.request.ConfirmProfilePictureRequest
import com.mobidev.studio.chirp.api.util.requestUserId
import com.mobidev.studio.chirp.service.ChatParticipantService
import com.mobidev.studio.chirp.service.ProfilePictureService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/chat/participants")
class ChatParticipantController(
    private val chatParticipantService: ChatParticipantService,
    private val profilePictureService: ProfilePictureService
) {

    @GetMapping
    fun getChatParticipantByUsernameOrEmail(
        // nullable query will return chat participant for user sessions ->
        // used for account details
        @RequestParam(required = false) query: String?
    ): ChatParticipantDto {
        val participant = if (query == null) {
            chatParticipantService.findChatParticipantById(requestUserId)
        } else {
            chatParticipantService.findChatParticipantByEmailOrUsername(query)
        }

        return participant?.toChatParticipantDto()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/profile-picture-upload")
    fun generateUploadCredentials(
        @RequestParam mimeType: String
    ): PictureUploadDto {
        return profilePictureService.generateUploadCredentials(
            userId = requestUserId,
            mimeType = mimeType
        ).toDto()
    }

    @PostMapping("/profile-picture-confirm")
    fun confirmProfilePictureUpload(
        @Valid @RequestBody body: ConfirmProfilePictureRequest
    ) {
        profilePictureService.confirmProfilePictureUpload(
            userId = requestUserId,
            publicUrl = body.publicUrl
        )
    }

    @DeleteMapping("/profile-picture-delete")
    fun deleteProfilePicture() {
        profilePictureService.deleteProfilePicture(
            userId = requestUserId
        )
    }
}