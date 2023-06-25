package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class ResultNode(nf: JsonNodeFactory) : ArrayNode(nf) {

    val indexes: Set<Int> get() {
        val set = mutableSetOf<Int>()
        filterIsInstance<RangeNode>().forEach { range ->
            set.addAll(range.indexes)
        }
        return set.sorted().toSet()
    }

    val value: JsonNode?
        get() {
            return when (size()) {
                0 -> null
                1 -> this[0]
                else -> this
            }
        }

    override fun add(node: JsonNode?): ResultNode {
        if (node != null) when (node) {
            is ArrayNode -> super.addAll(node)
            else -> super.add(node)
        }
        return this
    }

} //~ Results