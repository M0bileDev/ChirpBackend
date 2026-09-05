package com.mobidev.studio.chirp.service

import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context


@Service
class EmailTemplateService(
    private val templateEngine: TemplateEngine
) {
    // this template will be attached to spring email client as send via MailGun
    fun processTemplate(
        templateName: String,
        variables: Map<String, Any>
    ): String {
        val context = Context().apply {
            variables.forEach { (key, value) ->
                setVariable(key, value)
            }
        }

        return templateEngine.process(templateName, context)
    }
}