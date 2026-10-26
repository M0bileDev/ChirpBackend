package com.mobidev.studio.chirp.api.ws

import com.mobidev.studio.chirp.domain.type.ChatId
import com.mobidev.studio.chirp.domain.type.UserId
import com.mobidev.studio.chirp.service.ChatMessageService
import com.mobidev.studio.chirp.service.ChatService
import com.mobidev.studio.chirp.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
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
}