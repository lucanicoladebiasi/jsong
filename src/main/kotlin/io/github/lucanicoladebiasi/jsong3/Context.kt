package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.MathContext

data class Context(
    val loop: Loop?,
    val node: JsonNode?,
    val pmap: MutableMap<JsonNode, JsonNode?>,
    var vars: MutableMap<String, JsonNode>,
    val om: ObjectMapper,
    val mc: MathContext,
) {

    fun createArrayNode(): ArrayNode {
        return om.createArrayNode()
    }

    fun createBindContextNode(): BindContextNode {
        return BindContextNode(om)
    }

    fun createBindPositionNode(): BindPositionNode {
        return BindPositionNode(om)
    }

    fun createObjectNode(): ObjectNode {
        return om.createObjectNode()
    }

    fun pmap(parent: JsonNode, child: JsonNode): JsonNode {
        pmap[child] = parent
        return child
    }

    data class Loop(
        val size: Int,
        var index: Int = 0
    )

} //~ Context