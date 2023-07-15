package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions

open class Library {

    companion object {

        private val map = mutableMapOf<String, Set<Signature>>()

        fun call(name: String, args: MutableList<JsonNode?>): JsonNode? {
            when (val set = map[name]) {
                null -> throw NoSuchMethodException("$name function not found")
                else -> return match(set, args)?.call(args)
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        private fun match(parameters: List<KParameter>, args: List<JsonNode?>): Boolean {
            when (args.size == parameters.filter { !it.isOptional }.size - 1) {
                true -> {
                    for (i in args.indices) {
                        if (args[i] != null) {
                            if(!args[i]!!::class.createType().isSubtypeOf(parameters[i + 1].type)) {
                                return false
                            }
                        }
                    }
                    return true
                }

                else -> return false
            }
        }

        private fun match(signatures: Set<Signature>, args: List<JsonNode?>): Signature? {
            return signatures.firstOrNull() {
                match(it.callable.parameters, args)
            }
        }

        fun register(library: Library): Companion {
            val raw = mutableMapOf<String, MutableList<Signature>>()
            library::class.memberFunctions.forEach {
                raw.getOrPut(it.name) { mutableListOf() }.add(Signature(library, it))
            }
            raw.forEach { (name, signatures) ->
                map[name] = signatures.sortedByDescending { it.callable.parameters.size }.toSet()
            }
            return this
        }

    } //~ companion


}