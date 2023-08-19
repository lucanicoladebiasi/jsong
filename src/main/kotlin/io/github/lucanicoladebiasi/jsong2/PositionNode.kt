package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class PositionNode(nf: JsonNodeFactory) : ArrayNode(nf) {

    fun resolve(context: JsonNode?): IntNode? {
        if (context != null) forEach { node ->
            if (node is BindNode && node.value == context) {
                return node.pos
            }
        }
        return null
    }

} //~ PositionalNode
