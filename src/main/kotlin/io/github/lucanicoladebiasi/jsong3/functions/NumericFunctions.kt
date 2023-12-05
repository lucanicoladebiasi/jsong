package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import java.lang.IllegalArgumentException
import java.math.BigDecimal

class NumericFunctions {

    companion object {

        const val BIN_TAG = "0b"

        const val HEX_TAG = "0x"

        const val OCT_TAG = "0o"

        @Throws(IllegalArgumentException::class)
        fun decimalOf(node: JsonNode?): BigDecimal {
            return when(node) {
                null -> BigDecimal.ZERO
                is BooleanNode -> when(node.booleanValue()) {
                    true -> BigDecimal.ONE
                    else -> BigDecimal.ZERO
                }
                is NumericNode -> node.decimalValue()
                is TextNode -> {
                    val txt = node.textValue()
                    when {
                        txt.startsWith(BIN_TAG) -> txt.substring(BIN_TAG.length).toBigDecimal()
                        txt.startsWith(OCT_TAG) -> txt.substring(OCT_TAG.length).toBigDecimal()
                        txt.startsWith(HEX_TAG) -> txt.substring(HEX_TAG.length).toBigDecimal()
                        else -> txt.toBigDecimal()
                    }
                }
                else -> throw IllegalArgumentException("arg $node is not a number")

                // --5
            }
        }

    } //~ companion

} //~ NumericFunctions