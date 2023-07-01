package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode

class Context(
    val node: JsonNode,
    val prev: Context? = null,
) {

    fun back(step: Int): Context? {
        return when {
            step <= 0 -> this
            else -> prev?.back(step - 1)
        }
    }

    operator fun get(index: Int): Context? {
        when (node) {
            is ArrayNode -> {
                val offset = if (index < 0) node.size() + index else index
                if (offset in 0 until node.size()) {
                    return Context(node[offset], this)
                }
            }

            else -> if (index == 0) {
                return Context(node)
            }
        }
        return null
    }

}