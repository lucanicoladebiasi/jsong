package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode

class BindPositionNode(mapper: ObjectMapper) : ArrayNode(mapper.nodeFactory) {

    fun get(loop: Context.Loop): JsonNode? {
        return get(loop.index)
    }

} //~ BindPositionNode