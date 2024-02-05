package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.lucanicoladebiasi.jsong3.functions.Library
import java.math.MathContext
import java.time.Instant
import kotlin.random.Random

data class Context(
    val lib: Library,
    val loop: Loop?,
    val mc: MathContext,
    val node: JsonNode?,
    val now: Instant,
    val om: ObjectMapper,
    val pmap: MutableMap<JsonNode, JsonNode?>,
    val rand: Random,
    var vars: MutableMap<String, JsonNode?>,
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