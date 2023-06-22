package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal

class RangeNode private constructor(
    min: DecimalNode,
    max: DecimalNode,
    mapper: ObjectMapper
) : ObjectNode(
    mapper.nodeFactory,
    mapOf<String, DecimalNode>(Pair(MAX_TAG, max), Pair(MIN_TAG, min))
) { //~ RangeNode

    companion object {

        const val MAX_TAG = "max"

        const val MIN_TAG = "min"

        fun between(
            x: BigDecimal,
            y: BigDecimal,
            mapper: ObjectMapper = ObjectMapper()
        ): RangeNode {
            return RangeNode(DecimalNode(x.min(y)), DecimalNode(x.max(y)), mapper)
        }

    } //~ companion

    val max get() = this[io.github.lucanicoladebiasi.jsong1.RangeNode.MAX_TAG] as DecimalNode

    val min get() = this[io.github.lucanicoladebiasi.jsong1.RangeNode.MIN_TAG] as DecimalNode

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

} //~ RangeNode