package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal

class Functions(private val mapper: ObjectMapper) {

    fun array(node: JsonNode?): ArrayNode {
        return when (node) {
            null -> mapper.createArrayNode()
            is ArrayNode -> node
            else -> mapper.createArrayNode().add(node)
        }
    }

    private fun boolean(array: ArrayNode): Boolean {
        if (!array.isEmpty) {
            array.forEach { node ->
                if (boolean(node).booleanValue()) return true
            }
        }
        return false
    }

    /**
     *
     */
    fun boolean(arg: JsonNode?): BooleanNode {
        return when (arg) {
            null -> BooleanNode.FALSE
            is ArrayNode -> BooleanNode.valueOf(boolean(arg))
            is BooleanNode -> arg
            is DecimalNode -> BooleanNode.valueOf(arg.decimalValue() != BigDecimal.ZERO)
            is NullNode -> BooleanNode.FALSE
            is ObjectNode -> BooleanNode.valueOf(!arg.isEmpty)
            else -> BooleanNode.valueOf(!arg.asText().isNullOrEmpty())
        }
    }

    fun eq(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(flatten(lhs) == flatten(rhs))
    }

    fun flatten(node: JsonNode?): JsonNode? {
        return when (node) {
            is RangeNodes -> when (node.size()) {
                0 -> null
                1 -> flatten(node[0]) as RangeNode
                else -> {
                    val res = RangeNodes(mapper.nodeFactory)
                    node.forEach { element ->
                        flatten(element)?.let { res.add(it as RangeNode) }
                    }
                    res
                }
            }
            is ArrayNode -> when (node.size()) {
                0 -> null
                1 -> flatten(node[0])
                else -> {
                    val res = mapper.createArrayNode()
                    node.forEach { element ->
                        flatten(element)?.let { res.add(it) }
                    }
                    res
                }
            }
            else -> node
        }
    }

    fun ge(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(
            when (val lhn = flatten(lhs)) {
                null -> false
                else -> when (val rhn = flatten(rhs)) {
                    null -> false
                    is DecimalNode -> lhn.bigIntegerValue() >= rhn.bigIntegerValue()
                    is NumericNode -> lhn.bigIntegerValue() >= rhn.bigIntegerValue()
                    else -> lhn.asText() > rhn.asText()
                }
            }
        )
    }

    fun gt(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(
            when (val lhn = flatten(lhs)) {
                null -> false
                else -> when (val rhn = flatten(rhs)) {
                    null -> false
                    is DecimalNode -> lhn.bigIntegerValue() > rhn.bigIntegerValue()
                    is NumericNode -> lhn.bigIntegerValue() > rhn.bigIntegerValue()
                    else -> lhn.asText() > rhn.asText()
                }
            }
        )
    }

    fun include(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        val lhs = flatten(lhs)
        val rhs = array(flatten(rhs))
        return BooleanNode.valueOf(rhs.contains(lhs))
    }

    fun le(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(when (val lhn = flatten(lhs)) {
            null -> false
            else -> when (val rhn = flatten(rhs)) {
                null -> false
                is DecimalNode -> lhn.bigIntegerValue() <= rhn.bigIntegerValue()
                is NumericNode -> lhn.bigIntegerValue() <= rhn.bigIntegerValue()
                else -> lhn.asText() > rhn.asText()
            }
        })
    }

    fun lt(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(when (val lhn = flatten(lhs)) {
            null -> false
            else -> when (val rhn = flatten(rhs)) {
                null -> false
                is DecimalNode -> lhn.bigIntegerValue() < rhn.bigIntegerValue()
                is NumericNode -> lhn.bigIntegerValue() < rhn.bigIntegerValue()
                else -> lhn.asText() > rhn.asText()
            }
        })
    }

    fun ne(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(flatten(lhs) != flatten(rhs))
    }

}