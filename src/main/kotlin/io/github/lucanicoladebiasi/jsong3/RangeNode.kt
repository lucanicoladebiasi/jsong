package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal

class RangeNode(
    om: ObjectMapper,
    min: DecimalNode,
    max: DecimalNode
) : ObjectNode(om.nodeFactory, mapOf<String, DecimalNode>(Pair(MAX_TAG, max), Pair(MIN_TAG, min))) {

    companion object {

        const val MAX_TAG = "max"

        const val MIN_TAG = "min"

        fun between(
            x: BigDecimal,
            y: BigDecimal,
            mapper: ObjectMapper
        ): RangeNode {
            return RangeNode(mapper, DecimalNode(x.min(y)), DecimalNode(x.max(y)))
        }

        private fun isIndexer(array: ArrayNode): Boolean {
            array.forEach { element ->
                if (element !is NumericNode && element !is RangeNode) return false
            }
            return true
        }

        fun indexes(node: JsonNode?): Set<Int> {
            val indexes = mutableSetOf<Int>()
            when (node) {
                is ArrayNode -> if (isIndexer(node)) node.forEach { element ->
                    indexes.addAll(indexes(element))
                }

                is NumericNode -> indexes.add(node.intValue())
                is RangeNode -> indexes.addAll(node.indexes)
            }
            return indexes.sorted().toSet()
        }

    } //~ companion

    val indexes: Set<Int> get() = (min.asInt()..max.asInt()).toSet()

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

} //~ RangeNode