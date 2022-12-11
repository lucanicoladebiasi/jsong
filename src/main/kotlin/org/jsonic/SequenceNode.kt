package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class SequenceNode(
    nf: JsonNodeFactory
) : ArrayNode(nf) {

    override fun add(node: JsonNode?): SequenceNode {
        if (node != null) {
            when(node) {
                is ArrayNode -> node.forEach { element ->
                    add(element)
                }
                else -> super.add(node)
            }
        }
        return this
    }


}