package com.malcolmcrum.typescriptwebsocketapi.demo.services

import com.malcolmcrum.typescriptwebsocketapi.demo.api.ChatApi
import com.malcolmcrum.typescriptwebsocketapi.demo.api.ChatEvents
import com.malcolmcrum.typescriptwebsocketapi.demo.dtos.State
import com.malcolmcrum.typescriptwebsocketapi.demo.dtos.StoredMessage
import io.javalin.websocket.WsCloseContext
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsContext
import org.eclipse.jetty.websocket.api.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger(ChatService::class.java)

class ChatService(private val ctx: WsContext, private val clientProxy: ChatEvents) : ChatApi {

    companion object {
        // Persistent data for people that join the chatroom late
        val clients = mutableMapOf<Session, String>()
        val messages = mutableListOf<StoredMessage>()
    }

    override fun join(username: String) {
        log.info("Client logged in as $username")
        // Send a message just to the client that joined with the current state
        clientProxy.onInitialJoin(username, State(clients.values.toList(), messages))
        clients[ctx.session] = username
        // Send a message to everyone, so they can update their user lists
        clientProxy.onUserJoined(username)
    }

    override fun send(message: String) {
        val from = clients[ctx.session] ?: throw Error("Not logged in")
        messages.add(StoredMessage(from, message))
        clientProxy.onMessage(message, from)
    }

    override fun sendPrivate(to: String, message: String) {
        val from = clients[ctx.session] ?: throw Error("Not logged in")
        clientProxy.onPrivateMessage(to, message, from)
    }

    override fun onConnect(ctx: WsConnectContext) {
        log.info("Client connected")
    }

    override fun onClose(ctx: WsCloseContext) {
        val username = clients.remove(ctx.session)
        log.info("Client $username disconnected")
        if (username != null) {
            clientProxy.onUserLeft(username)
        }
    }

}