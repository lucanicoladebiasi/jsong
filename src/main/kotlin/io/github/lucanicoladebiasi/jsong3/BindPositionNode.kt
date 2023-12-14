package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

class BindPositionNode(mapper: ObjectMapper): ObjectNode(mapper.nodeFactory) {

    override fun get(index: Int): JsonNode? {
        return get(index.toString())
    }

    fun set(index: Int, value: JsonNode): BindPositionNode {
        return set(index.toString(), value)
    }

} //~ BindPositionNode