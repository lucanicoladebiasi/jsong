package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions

open class Library {

    private val map = mutableMapOf<String, Set<Function>>()

    fun call(name: String, args: List<JsonNode?>, context: JsonNode?): JsonNode? {
        map[name]?.let { signatures ->
            val arguments = mutableListOf<JsonNode?>()
            if (args.size < signatures.last().callable.parameters.size - 1) {
                arguments.add(context)
            }
            arguments.addAll(args)
            signatures.filter { arguments.size == it.callable.parameters.size - 1 }.forEach { signature ->
                matching@ for (i in arguments.indices) {
                    if (arguments[i] != null && !arguments[i]!!::class.createType()
                            .isSubtypeOf(signature.callable.parameters[i + 1].type)
                    ) {
                        continue@matching
                    }
                }
                return signature.callable.call(signature.instance, *arguments.toTypedArray()) as JsonNode?
            }
            throw NoSuchMethodException("$name($arguments) not found")
        }
        throw NoSuchMethodException("$name not found")
    }

    fun register(instance: Any): Library {
        val map = mutableMapOf<String, MutableList<Function>>()
        instance::class.memberFunctions.forEach {
            map.getOrPut(it.name) { mutableListOf() }.add(Function(instance, it))
        }
        map.forEach { (name, functions) ->
            this.map[name] = functions.sortedByDescending { it.callable.parameters.size }.toSet()
        }
        return this
    }

} //~ Library