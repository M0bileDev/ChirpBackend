package com.mobidev.studio.chirp.infra.storage

import com.mobidev.studio.chirp.api.dto.SignedUploadDto
import com.mobidev.studio.chirp.domain.exception.InvalidProfilePictureException
import com.mobidev.studio.chirp.domain.exception.StorageException
import com.mobidev.studio.chirp.domain.model.ProfilePictureUploadCredentials
import com.mobidev.studio.chirp.domain.type.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.*

@Service
class SupabaseStorageService(
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    private val supabaseRestClient: RestClient
) {

    private fun createSignedUrl(
        path: String,
        expiresInSeconds: Long
    ): String {
        val json = """
            { "expiresIn": $expiresInSeconds }
        """.trimIndent()
        val response = supabaseRestClient
            .post()
            .uri("/storage/v1/object/upload/sign/$path")
            // information when upload url expires
            .body(json)
            // retrieve the response
            .retrieve()
            // map to dto model
            .body(SignedUploadDto::class.java)
            ?: throw StorageException("Failed to create signed url")

        return "$supabaseUrl/storage/v1${response.url}"
    }

    fun generateSignedUploadUrl(userId: UserId, mimeType: String): ProfilePictureUploadCredentials {
        val extension =
            supportedMimeTypes[mimeType] ?: throw InvalidProfilePictureException("Unsupported mime type $mimeType")
        val fileName = "user_${userId}_${UUID.randomUUID()}.$extension"
        val path = "profile-pictures/$fileName"
        val publicUrl = "$supabaseUrl/storage/v1/object/public/$path"

        return ProfilePictureUploadCredentials(
            uploadUrl = createSignedUrl(
                path = path,
                expiresInSeconds = UPLOAD_URL_EXPIRATION_TIME
            ),
            publicUrl = publicUrl,
            headers = mapOf(
                CONTENT_TYPE to extension
            ),
            expiresAt = Instant.now().plusSeconds(UPLOAD_URL_EXPIRATION_TIME)
        )
    }

    fun deleteFile(
        url: String
    ) {
        val path = if (url.contains(OBJECT_PATH)) {
            url.substringAfter(OBJECT_PATH)
        } else throw StorageException("Invalid file URL format")
        val deleteUrl = "storage/v1/object/$path"

        val response = supabaseRestClient
            .delete()
            .uri(deleteUrl)
            .retrieve()
            // response without a body
            .toBodilessEntity()

        if (response.statusCode.isError) {
            throw StorageException("Unable to delete file, issue: ${response.statusCode.value()}")
        }
    }

    companion object {
        private val supportedMimeTypes = mapOf(
            "image/jpeg" to "image/jpg",
            "image/jpg" to "image/jpg",
            "image/png" to "image/png",
            "image/webp" to "image/webp",
        )
        private const val CONTENT_TYPE = "Content-Type"
        private const val UPLOAD_URL_EXPIRATION_TIME = 300L
        private const val OBJECT_PATH = "/object/public/"
    }
}