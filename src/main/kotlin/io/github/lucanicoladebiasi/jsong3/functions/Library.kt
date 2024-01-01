package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.IntNode
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions

open class Library {

    private val map = mutableMapOf<String, MutableMap<List<KType>, Handler>>()

    private class Handler(
        val function: KFunction<*>,
        val instance: Any,
        val signature: List<KType>
    ) {

        fun call(vararg args: JsonNode?): Any? {
            return function.call(instance, *args)
        }

        fun isCompatible(vararg args: JsonNode?): Boolean {
            val arguments = args.toList()
            if (arguments.size == signature.size) repeat(arguments.size) { i ->
                arguments[i]?.let { argument ->
                    if (!argument::class.createType().isSubtypeOf(signature[i])) {
                        return false
                    }
                }
            }
            return true
        }

    } //~ Handler

    @Throws(Exception::class)
    fun call(name: String, vararg args: JsonNode?): JsonNode? {
        map[name]?.let { handlers ->
            handlers.forEach { (_, handler) ->
                if (handler.isCompatible(*args)) {
                    return handler.call(*args) as JsonNode?
                }
            }
            throw NoSuchMethodException("function $name($args) not found")
        }
        throw NoSuchMethodException("function $name not found")
    }

    fun register(instance: Any): Library {
        instance::class.memberFunctions
            .filter { f ->
                f.hasAnnotation<Function>()
            }
            .sortedWith { f1, f2 ->
                when (val c = f1.name.compareTo(f2.name)) {
                    0 -> f2.parameters.size.compareTo(f1.parameters.size)
                    else -> c
                }
            }
            .forEach { f ->
                val signature = f.parameters.slice(1 until f.parameters.size).map { p -> p.type }
                val handlers = map.getOrDefault(f.name, mutableMapOf())
                handlers[signature] = Handler(f, instance, signature)
                map[f.name] = handlers
            }
        return this
    }

} //~ Library

fun main() {
    val lib = Library().register(NumericFunctions)
    val v = lib.call("abs", IntNode(-1))
    println(v)
}

