package com.mobidev.studio.chirp.service

import com.mobidev.studio.chirp.domain.type.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateService: EmailTemplateService,
    @param:Value("\${chirp.email.from}") private val emailFrom: String,
    @param:Value("\${chirp.email.url}") private val baseUrl: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendVerificationEmail(
        email: String,
        username: String,
        userId: UserId,
        token: String
    ) {
        logger.info("Sending verification email for user id: $userId...")

        val verificationUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/v1/auth/verify")
            .queryParam("token", token)
            .build()
            .toUriString()

        val htmlContent = templateService.processTemplate(
//            template name has to be identical as placed in the resources
            templateName = "emails/account-verification",
            variables = mapOf(
                "username" to username,
                "verificationUrl" to verificationUrl,
            )
        )


    }
}