package com.malcolmcrum.typescriptwebsocketapi.demo

import com.malcolmcrum.typescriptwebsocketapi.WebsocketServiceHandler
import com.malcolmcrum.typescriptwebsocketapi.demo.api.ChatApi
import com.malcolmcrum.typescriptwebsocketapi.demo.api.ChatEvents
import com.malcolmcrum.typescriptwebsocketapi.demo.services.ChatService
import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.http.staticfiles.Location

fun main() {
    val app = Javalin.create { cfg: JavalinConfig ->
        cfg.addStaticFiles("frontend/src", Location.EXTERNAL)
    }
    val chatServices = WebsocketServiceHandler(
        app,
        ChatApi::class.java,
        ChatEvents::class.java
    )
    val clientProxy = chatServices.createClientProxy()
    chatServices.createServerRoutes { ctx -> ChatService(ctx, clientProxy) }
    app.start()
}