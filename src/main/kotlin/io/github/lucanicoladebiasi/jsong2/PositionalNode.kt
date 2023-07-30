package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class PositionalNode(nf: JsonNodeFactory) : ArrayNode(nf) {

    fun position(node: JsonNode?): IntNode? {
        return if (node != null) {
            val index = this.indexOf(node)
            if (index < 0) null else IntNode(index)
        } else null
    }

}