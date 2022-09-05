package org.jsong


import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class RangeNodes constructor(nodeFactory: JsonNodeFactory) : ArrayNode(nodeFactory) {

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

    fun add(value: RangeNode?): RangeNodes {
        return super.add(value) as RangeNodes
    }

    fun addAll(nodes: MutableCollection<out RangeNode>?): RangeNodes {
        return super.addAll(nodes) as RangeNodes
    }

    fun addAll(other: RangeNodes?): RangeNodes {
        return super.addAll(other) as RangeNodes
    }

}