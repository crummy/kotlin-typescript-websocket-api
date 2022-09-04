package com.malcolmcrum.typescriptwebsocketapi.demo.api

import com.malcolmcrum.typescriptwebsocketapi.WebsocketApi

interface ChatApi : WebsocketApi {
    fun join(username: String)
    fun send(message: String)
    fun sendPrivate(to: String, message: String)
}