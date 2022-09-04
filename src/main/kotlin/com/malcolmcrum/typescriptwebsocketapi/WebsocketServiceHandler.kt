package com.malcolmcrum.typescriptwebsocketapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.Javalin
import io.javalin.websocket.*
import org.eclipse.jetty.websocket.api.Session
import org.slf4j.LoggerFactory
import java.lang.reflect.*
import java.util.concurrent.ConcurrentHashMap

private val log = LoggerFactory.getLogger(WebsocketServiceHandler::class.java)

class WebsocketServiceHandler<T : WebsocketApi, V>(
    private val app: Javalin,
    private val serverService: Class<T>,
    private val clientService: Class<V>
) {
    private val REGISTER_MESSAGE_TYPE = "register"
    private val objectMapper = jacksonObjectMapper()
    private val eventListeners: MutableMap<String, MutableList<Session>> = ConcurrentHashMap<String, MutableList<Session>>()

    // This special proxy handler is used to create a fake "client". Calls to it are
    // sent as websocket messages to any listeners.
    fun createClientProxy(): V {
        val handler = InvocationHandler { _: Any?, method: Method, args: Array<Any?> ->
            val argsMap: MutableMap<String, JsonNode> = mutableMapOf()
            for (i in 0 until method.parameterCount) {
                argsMap[method.parameters[i].name] = objectMapper.valueToTree(args[i]!!)
            }
            val eventName = toEventName(method, argsMap)
            val message = ServerMessage(eventName, argsMap)
            val json: String = objectMapper.writeValueAsString(message)
            val listeners = eventListeners.getOrDefault(eventName, emptyList())
            for (session in listeners) {
                session.remote.sendString(json)
            }
            log.info("Forwarded message $eventName to ${listeners.size} listeners")
            null
        }
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(clientService.classLoader, arrayOf(clientService), handler) as V
    }

    fun createServerRoutes(serviceProvider: (ctx: WsContext) -> T) {
        val serviceName = serverService.simpleName
        val methods: Map<String, Method> = serverService.methods.associateBy { it.name }
        val path = "/services/websocket/$serviceName"
        app.ws(path) { cfg: WsConfig ->
            cfg.onConnect { ctx: WsConnectContext ->
                val service = serviceProvider(ctx)
                service.onConnect(ctx)
            }
            // Receive websocket messages, parse out arguments, and call the appropriate method
            cfg.onMessage { ctx: WsMessageContext ->
                val message: ClientMessage = objectMapper.readValue(ctx.message(), ClientMessage::class.java)
                // An exception to the normal behaviour are "register" messages. These
                // register listeners which will be notified when the proxy object above is called.
                if (message.type == REGISTER_MESSAGE_TYPE) {
                    val event: String = message.args["event"]?.asText() ?: throw IllegalArgumentException("No event specified")
                    val listeners = eventListeners.computeIfAbsent(event) { mutableListOf() }
                    listeners.add(ctx.session)
                    log.info("Listener registered for $event")
                } else {
                    val method: Method = methods[message.type] ?: throw Error("No method found for " + serviceName + "." + message.type)
                    val args = parseArgs(message.args, method, serviceName)
                    val service = serviceProvider(ctx)
                    log.info("Calling $serviceName.${method.name}(${args.joinToString(", ")})")
                    method.invoke(service, *args.toTypedArray() )
                }
            }
            cfg.onClose { ctx: WsCloseContext ->
                // Make sure we won't send any future events to the disconnected session
                for (listeners in eventListeners.values) {
                    listeners.remove(ctx.session)
                }
                val service = serviceProvider(ctx)
                service.onClose(ctx)
            }
            cfg.onError { ctx: WsErrorContext ->
                log.warn("Error encountered in websocket $serviceName", ctx.error())
            }
        }
        log.info("Set up websocket service on $path")
    }

    private fun toEventName(method: Method, argsMap: MutableMap<String, JsonNode>): String {
        // These @Filter parameters allow us to filter to just specific clients
        val uniqueParameters: String = method.parameters
            .filter { p -> p.isAnnotationPresent(Filter::class.java) }
            .joinToString("/") { p -> argsMap[p.name]!!.asText() }
        return method.name + if (uniqueParameters.isEmpty()) "" else "/$uniqueParameters"
    }

    private fun parseArgs(params: Map<String, JsonNode>, method: Method, serviceName: String): List<Any> {
        return method.parameters.map { param ->
            val paramName: String = param.name
            if ("arg0" == paramName) {
                throw RuntimeException("service class should be compiled with -parameters option.")
            } else if (!params.containsKey(paramName)) {
                throw RuntimeException("Couldn't find required param $paramName for $serviceName.${method.name} in provided params $params")
            }
            val type: Type = param.parameterizedType
            val clazz = (if (type is ParameterizedType) type.rawType else type) as Class<*>
            val value: Any = objectMapper.treeToValue( params[paramName]!!, clazz)
            value
        }
    }

    data class ClientMessage(val type: String, val args: Map<String, JsonNode>)

    data class ServerMessage(val event: String, val data: Map<String, JsonNode>)
}