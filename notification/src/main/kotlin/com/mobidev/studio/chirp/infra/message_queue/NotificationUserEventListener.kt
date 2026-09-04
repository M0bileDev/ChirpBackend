package com.mobidev.studio.chirp.infra.message_queue

import com.mobidev.studio.chirp.domain.events.user.UserEvent
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class NotificationUserEventListener {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> {
                println("User created!")
            }

            is UserEvent.RequestResendVerification -> {
                println("User requested resend verification!")
            }

            is UserEvent.RequestResetPassword -> {
                println("User requested password reset!")
            }

            else -> Unit
        }
    }
}