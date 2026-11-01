package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.domain.model.ProfilePictureUploadCredentials
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.infra.database.repositories.ChatParticipantRepository
import com.mobidev.studio.chirp.infra.storage.SupabaseStorageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

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
}