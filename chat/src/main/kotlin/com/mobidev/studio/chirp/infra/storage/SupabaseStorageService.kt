package com.mobidev.studio.chirp.infra.storage

import com.mobidev.studio.chirp.api.dto.SignedUploadDto
import com.mobidev.studio.chirp.domain.exception.StorageException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

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

    companion object {
        private val supportedMimeTypes = mapOf(
            "image/jpeg" to "image/jpg",
            "image/jpg" to "image/jpg",
            "image/png" to "image/png",
            "image/webp" to "image/webp",
        )
        private const val CONTENT_TYPE = "Content-Type"
        private const val UPLOAD_URL_EXPIRATION_TIME = 300L
    }
}