package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions

open class Library {

    private val map = mutableMapOf<String, List<Function>>()

    fun call(name: String, args: List<JsonNode?>, context: JsonNode?): JsonNode? {
        map[name]?.let { functions ->
            function_match_loop@ for (i in functions.indices) {
                val func = functions[i]
                val arguments = mutableListOf<JsonNode?>()
                if (args.size + 1 == func.callable.parameters.size - 1) {
                    arguments.add(context)
                }
                arguments.addAll(args)
                if (arguments.size == func.callable.parameters.size - 1) {
                    for (j in arguments.indices) {
                        val arg = arguments[j]
                        if (arg != null
                            && !arg::class.createType().isSubtypeOf(func.callable.parameters[j + 1].type)
                        ) {
                            continue@function_match_loop
                        }
                    }
                    return func.callable.call(func.instance, *arguments.toTypedArray()) as JsonNode?
                }
            }
        }
        throw NoSuchMethodException("$name not found")
    }

    fun register(instance: Any): Library {
        val map = mutableMapOf<String, MutableList<Function>>()
        instance::class.memberFunctions.forEach {
            map.getOrPut(it.name) { mutableListOf() }.add(Function(instance, it))
        }
        map.forEach { (name, functions) ->
            this.map[name] = functions.sortedByDescending { it.callable.parameters.size }
        }
        return this
    }

} //~ Library