package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class Results(nf: JsonNodeFactory) : ArrayNode(nf) {

    val value: JsonNode?
        get() {
            return when (size()) {
                0 -> null
                1 -> this[0]
                else -> this
            }
        }

    override fun add(node: JsonNode?): Results {
        if (node != null) when (node) {
            is ArrayNode -> super.addAll(node)
            else -> super.add(node)
        }
        return this
    }

} //~ Results