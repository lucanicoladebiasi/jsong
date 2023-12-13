package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.MathContext

data class Context(
    val node: JsonNode?,
    val loop: Int?,
    val mapper: ObjectMapper,
    val mathContext: MathContext,
    val variables: MutableMap<String, JsonNode>
) {

    constructor(node: JsonNode?, loop: Int?, context: Context) : this(
        node,
        loop,
        context.mapper,
        context.mathContext,
        context.variables
    )

    fun createArrayNode(): ArrayNode {
        return mapper.createArrayNode()
    }

    fun createObjectNode(): ObjectNode {
        return mapper.createObjectNode()
    }

}