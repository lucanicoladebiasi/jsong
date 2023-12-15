package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode

class BindPositionNode(mapper: ObjectMapper) : ArrayNode(mapper.nodeFactory) {

    override fun add(node: JsonNode?): BindPositionNode {
        if (node != null) when(node) {
            is ArrayNode -> addAll(node)
            else -> super.add(node)
        }
        return this
    }

    fun get(node: JsonNode?): IntNode? {
        if (node != null) {
            val index = indexOf(node)
            return if (index >= 0) IntNode(index) else null
        }
        return null
    }

} //~ BindPositionNode