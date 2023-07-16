package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.KCallable

data class Signature(val instance: Any, val callable: KCallable<*>) {

    fun call(args: List<JsonNode?>): JsonNode? {
        return callable.call(instance, *args.toTypedArray()) as JsonNode?
    }

}
