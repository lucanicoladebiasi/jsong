package org.jsong


import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class RangesNode constructor(nodeFactory: JsonNodeFactory) : ArrayNode(nodeFactory) {

    val indexes: ArrayNode
        get() {
            val set = mutableSetOf<Int>()
            forEach { node ->
                when (node) {
                    is RangeNode -> node.indexes.forEach {
                        when (it) {
                            is IntNode -> set.add(it.asInt())
                        }
                    }
                }
            }
            return _nodeFactory.arrayNode().addAll(set.sorted().map { IntNode(it) })
        }

}