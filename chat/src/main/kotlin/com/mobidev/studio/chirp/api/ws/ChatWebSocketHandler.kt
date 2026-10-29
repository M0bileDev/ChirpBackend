package com.mobidev.studio.chirp.api.ws

import com.mobidev.studio.chirp.api.dto.ChatParticipantsChangedDto
import com.mobidev.studio.chirp.api.dto.DeleteMessageDto
import com.mobidev.studio.chirp.api.dto.ErrorDto
import com.mobidev.studio.chirp.api.dto.SendMessageDto
import com.mobidev.studio.chirp.api.mappers.toChatMessageDto
import com.mobidev.studio.chirp.domain.event.ChatParticipantsJoinedEvent
import com.mobidev.studio.chirp.domain.event.ChatParticipantsLeftEvent
import com.mobidev.studio.chirp.domain.event.MessageDeletedEvent
import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.service.ChatMessageService
import com.mobidev.studio.chirp.service.ChatService
import com.mobidev.studio.chirp.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

// manages active connections, properly receives messages
// and sends them into clients
@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jwtService: JwtService,
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(javaClass)

    // enables multiple threads to read data at the same time, single thread to write data
    private val connectionLock = ReentrantReadWriteLock()

    private val sessions = ConcurrentHashMap<SessionId, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<SessionId>>()
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<SessionId>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        //only logged-in users can establish ws connection
        val authHeader = session
            // http handshake attempts to establish websocket connection
            .handshakeHeaders
            // header from which bearer token will be taken from
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} was closed due to missing Authorization header.")

                val closeStatus = CloseStatus.SERVER_ERROR.withReason("Authentication failed.")
                session.close(closeStatus)

                return
            }

        val userId = jwtService.getUserIdFromToken(authHeader)
        val userSession = UserSession(
            userId = userId,
            session = session,
        )

        // use only one thread to write
        connectionLock.write {

            //each operations below are using their own thread

            sessions[session.id] = userSession

            userToSessions.compute(userId) { _, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply {
                    add(session.id)
                }
            }

            val chatIds = userChatIds.computeIfAbsent(userId) {
                val chatIds = chatService.findChatsByUserId(userId).map { it.id }
                ConcurrentHashMap.newKeySet<ChatId>().apply { addAll(chatIds) }
            }

            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }
        }

        // connection established
        logger.info("Websocket connection established for user $userId.")
    }

    private fun sendTextMessageToUser(userId: UserId, message: OutgoingWebSocketMessage) {
        val userSessionsId = connectionLock.read {
            userToSessions[userId] ?: emptySet()
        }

        if (userSessionsId.isEmpty()) return

        userSessionsId.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId] ?: return@forEach
            }

            if (!userSession.session.isOpen) {
                try {
                    val messageJson = objectMapper.writeValueAsString(message)
                    val textMessage = TextMessage(messageJson)

                    userSession.session.sendMessage(textMessage)
                    logger.debug("Sent message to user {}: {}", userId, messageJson)
                } catch (ex: Exception) {
                    logger.error("Error while sending message to user $userId", ex)
                }
            }
        }
    }

    private fun broadcastToChat(
        chatId: ChatId,
        message: OutgoingWebSocketMessage
    ) {
        val chatSessions = connectionLock.read {
            chatToSessions[chatId]?.toList() ?: emptyList()
        }

        chatSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId]
            } ?: return@forEach

            sendTextMessageToUser(
                userId = userSession.userId,
                message = message,
            )
        }
    }

    private fun handleSendMessage(
        dto: SendMessageDto,
        senderId: UserId,
    ) {
        val userChatIds = connectionLock.read { userChatIds[senderId] } ?: return

        if (dto.chatId !in userChatIds) return

        val savedMessage = chatMessageService.sendMessage(
            chatId = dto.chatId,
            senderId = senderId,
            content = dto.content,
            messageId = dto.messageId
        )

        val payload = objectMapper.writeValueAsString(savedMessage.toChatMessageDto())
        val message = OutgoingWebSocketMessage(
            type = OutgoingWebSocketMessageType.NEW_MESSAGE,
            payload = payload,
        )

        broadcastToChat(
            chatId = dto.chatId,
            message = message
        )
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage
    ) {
        logger.debug("Received message ${message.payload}")

        val userSession = connectionLock.read {
            sessions[session.id] ?: return
        }

        try {
            val webSocketMessage = objectMapper.readValue(
                message.payload,
                IncomingWebSocketMessage::class.java
            )

            when (webSocketMessage.type) {
                IncomingWebSocketMessageType.NEW_MESSAGE -> {
                    val sendMessageDto = objectMapper.readValue(
                        webSocketMessage.payload,
                        SendMessageDto::class.java
                    )

                    handleSendMessage(
                        dto = sendMessageDto,
                        senderId = userSession.userId,
                    )
                }
            }
        } catch (
            ex: JacksonException
        ) {
            val errorDto = ErrorDto(
                code = "INVALID_MESSAGE",
                message = "Incoming JSON or UUID is not valid."
            )
            logger.error("Could not parse message ${message.payload}", ex)
            sendError(
                session = userSession.session,
                error = errorDto
            )
        }
    }

    private fun sendError(
        session: WebSocketSession,
        error: ErrorDto
    ) {
        val payload = objectMapper.writeValueAsString(error)
        val outgoingWebSocketMessage = OutgoingWebSocketMessage(
            type = OutgoingWebSocketMessageType.ERROR,
            payload = payload
        )
        val webSocketMessage = objectMapper.writeValueAsString(
            outgoingWebSocketMessage
        )

        try {
            val textMessage = TextMessage(webSocketMessage)

            session.sendMessage(textMessage)
        } catch (ex: Exception) {
            logger.error("Error while sending message", ex)
        }
    }

    // this listener executes after the value was published through
    // event like applicationEventPublisher.publishEvent(messageDeletedEvent), no additional dependencies needed
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDeleteMessage(messageDeletedEvent: MessageDeletedEvent) {

        val deleteMessageDto = DeleteMessageDto(
            chatId = messageDeletedEvent.chatId,
            messageId = messageDeletedEvent.messageId
        )
        val payload = objectMapper.writeValueAsString(deleteMessageDto)
        val message = OutgoingWebSocketMessage(
            type = OutgoingWebSocketMessageType.MESSAGE_DELETED,
            payload = payload
        )

        broadcastToChat(
            chatId = messageDeletedEvent.chatId,
            message = message
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onJoinChat(chatParticipantsJoinedEvent: ChatParticipantsJoinedEvent) {
        //first sync hashmaps
        connectionLock.write {
            chatParticipantsJoinedEvent.userIds.forEach { userId ->
                userChatIds.compute(userId) { _, chatIds ->
                    (chatIds ?: mutableSetOf()).apply {
                        add(chatParticipantsJoinedEvent.chatId)
                    }
                }

                userToSessions[userId]?.forEach { sessionId ->
                    chatToSessions.compute(chatParticipantsJoinedEvent.chatId) { _, sessionIds ->
                        (sessionIds ?: mutableSetOf()).apply { add(sessionId) }
                    }
                }
            }
        }

        //second, broadcast the change over ws, client will fetch changes over POST request (participant joined, left, etc.)
        broadcastChatParticipantsChanged(
            chatId = chatParticipantsJoinedEvent.chatId
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onLeftChat(chatParticipantsLeftEvent: ChatParticipantsLeftEvent) {
        //first sync hashmaps
        connectionLock.write {
            userChatIds.compute(chatParticipantsLeftEvent.userId) { _, chatIds ->
                chatIds
                    ?.apply { remove(chatParticipantsLeftEvent.chatId) }
                    ?.takeIf { it.isNotEmpty() }
            }

            userToSessions[chatParticipantsLeftEvent.chatId]?.forEach { sessionId ->
                chatToSessions.compute(chatParticipantsLeftEvent.chatId) { _, sessionIds ->
                    sessionIds
                        ?.apply { remove(sessionId) }
                        ?.takeIf { it.isNotEmpty() }
                }
            }
        }

        //second, broadcast the change over ws, client will fetch changes over POST request (participant joined, left, etc.)
        broadcastChatParticipantsChanged(
            chatId = chatParticipantsLeftEvent.chatId
        )
    }

    private fun broadcastChatParticipantsChanged(chatId: ChatId) {
        val dto = ChatParticipantsChangedDto(chatId = chatId)
        val payload = objectMapper.writeValueAsString(dto)
        val message = OutgoingWebSocketMessage(
            type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
            payload = payload
        )

        broadcastToChat(
            chatId = chatId,
            message = message
        )
    }
}