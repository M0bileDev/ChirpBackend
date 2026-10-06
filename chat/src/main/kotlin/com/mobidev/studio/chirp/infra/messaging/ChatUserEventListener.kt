package com.mobidev.studio.chirp.infra.messaging

import com.mobidev.studio.chirp.domain.events.user.UserEvent
import com.mobidev.studio.chirp.domain.model.ChatParticipant
import com.mobidev.studio.chirp.infra.message_queue.MessageQueues
import com.mobidev.studio.chirp.service.ChatParticipantService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(
    private val chatParticipantService: ChatParticipantService
) {

    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Verified -> {
                val chatParticipant = ChatParticipant(
                    userId = event.userId,
                    username = event.username,
                    email = event.email,
                    profilePicture = null
                )
                chatParticipantService.createChatParticipant(
                    chatParticipant = chatParticipant
                )
            }

            else -> Unit
        }
    }
}