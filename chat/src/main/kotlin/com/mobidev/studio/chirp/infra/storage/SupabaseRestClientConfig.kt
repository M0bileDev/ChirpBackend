package com.mobidev.studio.chirp.infra.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class SupabaseRestClientConfig(
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    @param:Value("\${supabase.service-key}") private val supabaseServiceKey: String,
) {

    @Bean
    fun supabaseRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(supabaseUrl)
            // every request will contain authorization header
            .defaultHeader(AUTHORIZATION_HEADER, "$BEARER_PREFIX $supabaseServiceKey")
            .build()
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer"
    }
}