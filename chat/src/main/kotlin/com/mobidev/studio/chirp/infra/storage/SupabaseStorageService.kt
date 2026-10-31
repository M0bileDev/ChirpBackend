package com.mobidev.studio.chirp.infra.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SupabaseStorageService(
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    private val supabaseRestClientConfig: SupabaseRestClientConfig
) {

}