package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberFunctions

class Library {

    companion object {

        private fun match(parameters: List<KParameter>, args: List<JsonNode?>): Boolean {
            return when(args.size == parameters.size - 1) {
                true -> {
                    for(i in args.indices) {
                        if (args[i] != null) {
                            println(parameters[i + 1].type)
                            println(args[i]!!::class)
                            println(parameters[i + 1].type == args[i]!!::class)
                        }
                    }
                    true
                }
                else -> false
            }
        }

        private fun match(signatures: Set<KCallable<*>>, args: List<JsonNode?>) {
            signatures.filter {
                match(it.parameters, args)
            }
        }

    }

    val map = mutableMapOf<String, Set<KCallable<*>>>()


    fun call(name: String, args: List<JsonNode?>): JsonNode? {
        when(val set = map[name]) {
            null -> throw NoSuchMethodException("$name function not found")
            else -> {
                match(set, args)
            }

        }
        return null
    }

    fun register(clazz: KClass<*>): Library {
        val raw = mutableMapOf<String, MutableList<KCallable<*>>>()
        clazz.memberFunctions.forEach {
            raw.getOrPut(it.name) { mutableListOf() }.add(it)
        }
        raw.forEach { (name, signares) ->
            map[name] = signares.sortedByDescending { it.parameters.size }.toSet()
        }
        return this
    }

}