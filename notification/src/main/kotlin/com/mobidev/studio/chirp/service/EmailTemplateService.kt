package com.mobidev.studio.chirp.service

import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine


@Service
class EmailTemplateService(
    private val templateEngine: TemplateEngine
) {

}