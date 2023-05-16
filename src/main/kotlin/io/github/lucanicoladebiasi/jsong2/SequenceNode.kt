package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class SequenceNode(
    private val nf: JsonNodeFactory
) : ArrayNode(nf) {

    val flatten: ArrayNode get() = flatten(this)

    val value: JsonNode? get() {
        val node = flatten
        return when (node.size()) {
            0 -> null
            1 -> node[0]
            else -> node
        }
    }

    fun append(node: JsonNode?): SequenceNode {
        when (node) {
            is SequenceNode -> addAll(node)
            is ArrayNode -> add(node)
            else -> add(SequenceNode(nf).add(node))
        }
        return this
    }

    private fun flatten(array: ArrayNode): ArrayNode {
        val flat = nf.arrayNode()
        array.forEach {
            when (it) {
                is ArrayNode -> flat.addAll(flatten(it))
                else -> flat.add(it)
            }
        }
        return flat
    }

} //~ SequenceNode

