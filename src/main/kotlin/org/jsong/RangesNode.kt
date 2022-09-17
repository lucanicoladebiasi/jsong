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

    fun add(value: RangeNode?): RangesNode {
        return super.add(value) as RangesNode
    }

    fun addAll(nodes: MutableCollection<out RangeNode>?): RangesNode {
        return super.addAll(nodes) as RangesNode
    }

    fun addAll(other: RangesNode?): RangesNode {
        return super.addAll(other) as RangesNode
    }

}