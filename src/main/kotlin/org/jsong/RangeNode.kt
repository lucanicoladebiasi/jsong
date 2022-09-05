package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal

class RangeNode private constructor(
    min: DecimalNode,
    max: DecimalNode,
    nodeFactory: JsonNodeFactory
) : ObjectNode(nodeFactory, mapOf<String, DecimalNode>(Pair(MAX_TAG, max), Pair(MIN_TAG, min))) {

    companion object {

        const val MAX_TAG = "max"

        const val MIN_TAG = "min"

        fun of(x: BigDecimal, y: BigDecimal, nodeFactory: JsonNodeFactory = ObjectMapper().nodeFactory): RangeNode {
            return RangeNode(DecimalNode(x.min(y)), DecimalNode(x.max(y)), nodeFactory)
        }

        fun indexes(node: JsonNode?, nodeFactory: JsonNodeFactory = ObjectMapper().nodeFactory): ArrayNode {
            val set = mutableSetOf<JsonNode>()
            when (node) {
                is ArrayNode -> node.forEach {
                    set.addAll(indexes(it, nodeFactory))
                }
                is DecimalNode -> set.add(IntNode(node.intValue()))
                is NumericNode -> set.add(IntNode(node.intValue()))
                is RangeNode -> for (i in node.min.asInt()..node.max.asInt()) {
                    set.add(IntNode(i))
                }
            }
            return nodeFactory.arrayNode().addAll(set.sortedBy { it.intValue() })
        }

    } //~ companion

    val max get() = this[MAX_TAG] as DecimalNode

    val min get() = this[MIN_TAG] as DecimalNode

    val indexes get() = indexes(this, _nodeFactory)

    fun contains(value: BigDecimal): Boolean {
        return (min.decimalValue() <= value) && (value <= max.decimalValue())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as RangeNode

        if (max != other.max) return false
        if (min != other.min) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + max.hashCode()
        result = 31 * result + min.hashCode()
        return result
    }

}