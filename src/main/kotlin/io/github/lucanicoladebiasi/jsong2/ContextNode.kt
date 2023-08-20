package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class ContextNode(nf: JsonNodeFactory): ArrayNode(nf) {

    fun resolve(index: Index?): JsonNode? {
        if (index != null) {
            return (this[index.value] as BindNode).value
        }
        return null
    }

}