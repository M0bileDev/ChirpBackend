package com.mobidev.studio.chirp.infra.message_queue

import com.mobidev.studio.chirp.domain.events.user.UserEvent
import com.mobidev.studio.chirp.service.EmailService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Component
class NotificationUserEventListener(
    private val emailService: EmailService
) {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> {
                emailService.sendVerificationEmail(
                    emailTo = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.verificationToken
                )
            }

            is UserEvent.RequestResendVerification -> {
                emailService.sendVerificationEmail(
                    emailTo = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.verificationToken
                )
            }

            is UserEvent.RequestResetPassword -> {
                emailService.sendPasswordResetEmail(
                    emailTo = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.passwordResetToken,
                    expiresIn = Duration.ofMinutes(event.expiresInMinutes)
                )
            }

            else -> Unit
        }
    }
}