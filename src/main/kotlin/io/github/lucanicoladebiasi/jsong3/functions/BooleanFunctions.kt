package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong3.RangeNode
import java.math.BigDecimal

class BooleanFunctions {

    companion object {

        fun booleanOf(node: JsonNode?): Boolean {
            return when (node) {
                null -> false
                is ArrayNode -> {
                    node.forEach { element ->
                        if (booleanOf(element))
                            return true
                    }
                    false
                }

                is BooleanNode -> node.booleanValue()
                is NumericNode -> node.decimalValue() != BigDecimal.ZERO
                is ObjectNode -> when (node) {
                    is RangeNode -> {
                        node.indexes.forEach { index ->
                            if (index != 0)
                                return true
                        }
                        false
                    }

                    else -> !node.isEmpty
                }

                is TextNode -> node.textValue().isNotEmpty()
                else -> false
            }
        }

    } //~ companion

} //~ BooleanFunctions