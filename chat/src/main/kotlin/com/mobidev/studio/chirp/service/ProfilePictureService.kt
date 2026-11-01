package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.domain.event.ProfilePictureUpdatedEvent
import com.mobidev.studio.chirp.domain.exception.ChatParticipantNotFoundException
import com.mobidev.studio.chirp.domain.model.ProfilePictureUploadCredentials
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import com.mobidev.studio.chirp.infra.storage.SupabaseStorageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfilePictureService(
    private val supabaseStorageService: SupabaseStorageService,
    private val chatParticipantsRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    fun generateUploadCredentials(
        userId: UserId,
        mimeType: String
    ): ProfilePictureUploadCredentials {
        return supabaseStorageService.generateSignedUploadUrl(
            userId = userId,
            mimeType = mimeType,
        )

    }

    @Transactional
    fun deleteProfilePicture(userId: UserId) {
        val participant = chatParticipantsRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId = userId)

        participant.profilePicture?.let { profilePictureUrl ->
            val updatedUser = participant.apply { profilePicture = null }
            chatParticipantsRepository.save(updatedUser)

            supabaseStorageService.deleteFile(profilePictureUrl)

            val event = ProfilePictureUpdatedEvent(
                userId = userId,
                newUrl = profilePictureUrl
            )
            applicationEventPublisher.publishEvent(
                event
            )
        }
    }
}