package com.malcolmcrum.typescriptwebsocketapi.typescriptgenerator

import com.malcolmcrum.typescriptwebsocketapi.Filter
import com.malcolmcrum.typescriptwebsocketapi.demo.api.ChatApi
import com.malcolmcrum.typescriptwebsocketapi.demo.api.ChatEvents
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.template.PebbleTemplate
import java.io.StringWriter
import java.io.Writer
import java.lang.reflect.Parameter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.collections.set

class TypeScriptWsGenerator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val services: Collection<Service> = listOf(
                Service(ChatApi::class.java, ChatEvents::class.java)
            )
            val output = toTypeScript(services)
            val root: Path = Paths.get(TypeScriptConverter::class.java.getResource("/")?.toURI() ?: throw Error()).parent
            val folder = root.resolve("ts")
            folder.toFile().mkdirs()
            val outputFile = folder.resolve("websocket-services.ts")
            Files.writeString(outputFile, output)
            println("Wrote websocket services to $outputFile")
        }
    }
}


private fun toTypeScript(services: Collection<Service>): String {
    val engine: PebbleEngine = PebbleEngine.Builder()
        .autoEscaping(false)
        .build()
    val compiledTemplate: PebbleTemplate = engine.getTemplate("websocket-services.ts.twig")
    val context: MutableMap<String, Any> = HashMap()
    context["services"] = services
    val writer: Writer = StringWriter()
    compiledTemplate.evaluate(writer, context)
    return writer.toString()
}

internal class Service(server: Class<*>, client: Class<*>) {
    val name: String = server.simpleName
    val serverMethods: List<Method> = server.methods.map { Method(it, server) }
        .filter { m: Method -> !ignoredMethods.contains(m.name) }
    val clientMethods: List<Method> = client.methods
        .map { Method(it, client) }

    companion object {
        private val ignoredMethods = setOf("onConnect", "onClose")
    }
}

class Method(m: java.lang.reflect.Method, service: Class<*>?) {
    val name: String = m.name
    val args: Collection<Argument> = m.parameters
        .filter { p: Parameter -> !p.isAnnotationPresent(Filter::class.java) }
        .map { p: Parameter -> Argument(p, service) }
    val filter: Collection<Argument> = m.parameters
        .filter { p: Parameter -> p.isAnnotationPresent(Filter::class.java) }
        .map { p: Parameter -> Argument(p, service) }
    val event: String = if (filter.isEmpty()) name else "$name/\${${filter.joinToString("/") { it.name }}}"
}

class Argument(t: Parameter, service: Class<*>?) {
    val name: String = t.name
    val type: String = TypeScriptConverter.getTsType(t.type)
}