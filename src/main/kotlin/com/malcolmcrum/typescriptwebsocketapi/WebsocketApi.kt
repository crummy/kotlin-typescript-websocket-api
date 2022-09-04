package com.malcolmcrum.typescriptwebsocketapi

import io.javalin.websocket.WsCloseContext
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsMessageContext

interface WebsocketApi {
    fun onConnect(ctx: WsConnectContext) 
    fun onClose(ctx: WsCloseContext)
    fun heartbeat() {
        // default does nothing, just to keep the connection alive
    }
}