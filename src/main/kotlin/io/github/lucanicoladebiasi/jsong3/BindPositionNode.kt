package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.ObjectNode

class BindPositionNode(private val om: ObjectMapper) : ArrayNode(om.nodeFactory) {

    class PositionNode(
        om: ObjectMapper,
        position: IntNode,
        value: JsonNode?
    ) : ObjectNode(om.nodeFactory, mapOf(Pair(POS_TAG, position), Pair(VAL_TAG, value))) {

        companion object {

            const val POS_TAG = "pos"

            const val VAL_TAG = "val"

        } //~ companion

        val position get() = this[POS_TAG] as IntNode

        val value get() = this[VAL_TAG]

    } //~ PositionNode

    fun add(position: Int, value: JsonNode): ArrayNode {
        add(PositionNode(om, IntNode(position), value))
        return this
    }

    fun get(loop: Context.Loop?): IntNode? {
        if (loop != null) {
            return when (val element = this[loop.index]) {
                is PositionNode -> element.position
                else -> null
            }
        }
        return null
    }

} //~ BindPositionNode