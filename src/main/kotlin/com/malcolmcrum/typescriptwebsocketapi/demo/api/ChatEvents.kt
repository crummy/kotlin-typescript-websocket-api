package com.malcolmcrum.typescriptwebsocketapi.demo.api

import com.malcolmcrum.typescriptwebsocketapi.Filter
import com.malcolmcrum.typescriptwebsocketapi.demo.dtos.State

interface ChatEvents {
    fun onMessage(message: String, from: String)
    fun onPrivateMessage(@Filter to: String, message: String, from: String)
    fun onInitialJoin(@Filter username: String, state: State)
    fun onUserJoined(name: String)
    fun onUserLeft(name: String)
}