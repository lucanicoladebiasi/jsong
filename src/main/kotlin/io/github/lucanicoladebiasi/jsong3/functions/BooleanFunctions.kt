package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong3.RangeNode
import io.github.lucanicoladebiasi.jsong3.Visitor
import java.math.BigDecimal

@Suppress("unused")
object BooleanFunctions {

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

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @LibraryFunction
    fun boolean(arg: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(booleanOf(arg))
    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @LibraryFunction
    fun not(arg: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(!booleanOf(arg))
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @LibraryFunction
    fun exists(arg: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(Visitor.reduce(arg) != null)
    }

} //~ BooleanFunctions