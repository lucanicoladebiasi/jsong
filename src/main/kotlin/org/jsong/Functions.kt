package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal

class Functions {

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
        return when(arg) {
            null -> BooleanNode.FALSE
            is ArrayNode -> BooleanNode.valueOf(boolean(arg))
            is BooleanNode -> arg
            is DecimalNode -> BooleanNode.valueOf(arg.decimalValue() != BigDecimal.ZERO)
            is NullNode -> BooleanNode.FALSE
            is ObjectNode -> BooleanNode.valueOf(!arg.isEmpty)
            else -> BooleanNode.valueOf(!arg.asText().isNullOrEmpty())
        }
    }
}