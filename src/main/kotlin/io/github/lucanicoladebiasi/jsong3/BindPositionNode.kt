package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.ObjectNode

class BindPositionNode(val mapper: ObjectMapper) : ArrayNode(mapper.nodeFactory) { //~ BindPositionNode

    class PositionNode(
        mapper: ObjectMapper,
        index: Int,
        value: JsonNode
    ) : ObjectNode(
        mapper.nodeFactory,
        mapOf<String, JsonNode>(Pair(POS_TAG, IntNode(index)), Pair(VAL_TAG, value))
    ) {

        companion object {

            const val POS_TAG = "pos"

            const val VAL_TAG = "val"

        } //~ companion

        val position get() = (this[POS_TAG] as IntNode)
        
        val value: JsonNode get() = this[VAL_TAG]

    } //~ PositionNode

    override fun add(value: JsonNode): BindPositionNode {
        super.add(PositionNode(mapper, size() + 1, value))
        return this
    }

    override fun addAll(other: ArrayNode?): BindPositionNode {
        other?.forEach { node -> add(node) }
        return this
    }

    override fun addAll(nodes: MutableCollection<out JsonNode>?): BindPositionNode {
        nodes?.forEach { node -> add(node) }
        return this
    }

} //~ BindPositionNode