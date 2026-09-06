package com.mobidev.studio.chirp.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateService: EmailTemplateService,
    @param:Value("\${chirp.email.from}") private val emailFrom: String,
    @param:Value("\${chirp.email.url}") private val baseUrl: String,
) {
}