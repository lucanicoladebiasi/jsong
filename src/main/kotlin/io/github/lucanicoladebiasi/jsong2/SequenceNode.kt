package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.TextNode

class SequenceNode(
    private val nf: JsonNodeFactory
) : ArrayNode(nf) {

    val value: JsonNode? get() = reduce(this)

    fun append(node: JsonNode?): SequenceNode {
        when(node) {
            null -> this
            is ArrayNode -> add(node)
            else -> add(SequenceNode(nf).add(node))
        }
        return this
    }

    fun reduce(node: JsonNode): JsonNode? {
        return when(node) {
            is ArrayNode -> when(node.size()) {
                0 -> null
                1 -> reduce(node[0])
                else -> {
                    val res = nf.arrayNode()
                    forEach {
                        res.add(reduce(it))
                    }
                    res
                }
            }
            else -> node
        }
    }

} //~ SequenceNode

