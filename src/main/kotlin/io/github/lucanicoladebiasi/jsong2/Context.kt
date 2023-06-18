package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode

data class Context(
    val node: JsonNode,
    val prev: Context? = null
) {

    fun back(steps: Int): Context {
        return when(prev == null) {
            true -> this
            else -> when(steps <= 0) {
                true -> this
                else -> prev.back(steps - 1)
            }
        }
    }

}