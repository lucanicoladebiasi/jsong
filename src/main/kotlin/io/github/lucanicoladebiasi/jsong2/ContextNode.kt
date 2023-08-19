package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class ContextNode(nf: JsonNodeFactory): ArrayNode(nf) {

    fun resolve(index: Int?): JsonNode? {
        if (index != null) forEach { node ->
            if (node is BindNode && node.pos.intValue() == index) {
                return node.value
            }
        }
        return null
    }

}