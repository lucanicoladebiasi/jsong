package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class SeqNode(
    private val nf: JsonNodeFactory
) : ArrayNode(nf) {


//    override fun add(node: JsonNode?): SequenceNode {
//        if (node != null) {
//            when (node) {
//                is ArrayNode -> node.forEach { element ->
//                    add(element)
//                }
//
//                else -> super.add(node)
//            }
//        }
//        return this
//    }

    override fun get(index: Int): JsonNode? {
        return if (index < 0) super.get(size() + index) else super.get(index)
    }


}