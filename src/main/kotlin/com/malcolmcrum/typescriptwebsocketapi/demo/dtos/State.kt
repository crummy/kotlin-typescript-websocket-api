package com.malcolmcrum.typescriptwebsocketapi.demo.dtos

data class State(val users: List<String>, val messages: List<StoredMessage>)

data class StoredMessage(val from: String, val content: String)