package com.mobidev.studio.chirp.infra.message_queue

import com.mobidev.studio.chirp.domain.events.ChirpEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.JacksonJsonMessageConverter
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.kotlinModule

@Configuration
class RabbitMqConfig {

    @Bean
    fun messageConverter(): JacksonJsonMessageConverter {
        // support serialization of subclasses
        val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(ChirpEvent::class.java)
            // allow Java lists
            .allowIfSubType("java.util.")
            // Kotlin collections
            .allowIfSubType("kotlin.collections.")
            .build()

        val objectMapper = JsonMapper.builder()
            // instruct Json to be able to serialize/deserialize Kotlin related data classes
            .addModule(kotlinModule())
            .polymorphicTypeValidator(polymorphicTypeValidator)
            .activateDefaultTyping(
                polymorphicTypeValidator,
                // for classes that are not final
                DefaultTyping.NON_FINAL
            )
            .build()
    }
}