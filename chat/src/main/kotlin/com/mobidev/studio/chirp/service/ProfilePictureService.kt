package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.domain.event.ProfilePictureUpdatedEvent
import com.mobidev.studio.chirp.domain.exception.ChatParticipantNotFoundException
import com.mobidev.studio.chirp.domain.model.ProfilePictureUploadCredentials
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import com.mobidev.studio.chirp.infra.storage.SupabaseStorageService
import org.slf4j.LoggerFactory
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

    val logger = LoggerFactory.getLogger(ProfilePictureService::class.java)

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
            val updatedParticipant = participant.apply { profilePicture = null }
            chatParticipantsRepository.save(updatedParticipant)

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

    // call as a result that client image have been uploaded successfully to the supabase db,
    // and profile url can be updated
    @Transactional
    fun confirmProfilePictureUpload(
        userId: UserId,
        publicUrl: String
    ) {
        val participant = chatParticipantsRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId = userId)
        val oldPictureUrl = participant.profilePicture

        val updatedParticipant = participant.apply { profilePicture = publicUrl }
        chatParticipantsRepository.save(updatedParticipant)

        try {
            oldPictureUrl?.let { url ->
                supabaseStorageService.deleteFile(url)
            }
        } catch (ex: Exception) {
            logger.error("Error while removing profile picture for user $userId", ex)
        }

        val event = ProfilePictureUpdatedEvent(
            userId = userId,
            newUrl = publicUrl
        )
        applicationEventPublisher.publishEvent(
            event
        )
    }
}