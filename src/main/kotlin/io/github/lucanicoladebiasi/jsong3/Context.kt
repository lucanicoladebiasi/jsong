package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.MathContext

data class Context(
    val node: JsonNode?,
    val loop: Loop?,
    val mapper: ObjectMapper,
    val mathContext: MathContext,
    var variables: MutableMap<String, JsonNode>
) {

    constructor(node: JsonNode?, loop: Loop?, context: Context) : this(
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

    data class Loop(
        val size: Int,
        val index: Int = 0
    ) {

        fun at(index: Int): Loop {
            return Loop(size, index)
        }

    }

} //~ Context