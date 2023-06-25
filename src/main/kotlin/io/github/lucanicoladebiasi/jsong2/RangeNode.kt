package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal

class RangeNode(
    min: DecimalNode,
    max: DecimalNode,
    nf: JsonNodeFactory
): ObjectNode(nf, mapOf<String, DecimalNode>(Pair(MAX_TAG, max), Pair(MIN_TAG, min))) {

    companion object {

        const val MAX_TAG = "max"

        const val MIN_TAG = "min"

        fun between(
            x: BigDecimal,
            y: BigDecimal,
            nf: JsonNodeFactory = ObjectMapper().nodeFactory
        ): RangeNode {
            return RangeNode(DecimalNode(x.min(y)), DecimalNode(x.max(y)), nf)
        }

    } //~ companion

    val indexes: Set<Int> get() = (min.asInt() .. max.asInt()).toSet()

    val max get() = (this[MAX_TAG] as DecimalNode)

    val min get() = this[MIN_TAG] as DecimalNode

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as RangeNode

        if (max != other.max) return false
        return min == other.min
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + max.hashCode()
        result = 31 * result + min.hashCode()
        return result
    }

}