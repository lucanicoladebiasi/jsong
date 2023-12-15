package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

class BindContextNode(mapper: ObjectMapper): ArrayNode(mapper.nodeFactory) {

    override fun add(node: JsonNode?): BindContextNode {
        if (node != null) when(node) {
            is ArrayNode -> addAll(node)
            else -> super.add(node)
        }
        return this
    }

}