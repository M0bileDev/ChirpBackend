package com.mobidev.studio.chirp.infra.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SupabaseStorageService(
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    private val supabaseRestClientConfig: SupabaseRestClientConfig
) {

    companion object {
        private val supportedMimeTypes = mapOf(
            "image/jpeg" to "image/jpg",
            "image/jpg" to "image/jpg",
            "image/png" to "image/png",
            "image/webp" to "image/webp",
        )
    }
}