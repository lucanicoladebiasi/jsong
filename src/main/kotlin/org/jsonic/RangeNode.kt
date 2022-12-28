package org.jsonic

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
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

    } //~ companion

    val max get() = this[MAX_TAG] as DecimalNode

    val min get() = this[MIN_TAG] as DecimalNode

    val indexes: ArrayNode get() {
        val array = _nodeFactory.arrayNode()
        for(i in min.asInt() .. max.asInt()) {
            array.add(IntNode(i))
        }
        return array
    }

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