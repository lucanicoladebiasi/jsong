package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.math.BigDecimal

@Suppress("unused")
object NumericFunctions {


    const val BIN_TAG = "0b"

    const val HEX_TAG = "0x"

    const val OCT_TAG = "0o"

    @Throws(IllegalArgumentException::class)
    fun decimalOf(node: JsonNode?): BigDecimal {
        return when (node) {
            null -> BigDecimal.ZERO
            is BooleanNode -> when (node.booleanValue()) {
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
        }
    }

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @LibraryFunction
    @Throws(IllegalArgumentException::class)
    fun number(arg: JsonNode): DecimalNode {
        return DecimalNode(decimalOf(arg))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#abs
     */
    @LibraryFunction
    fun abs(number: NumericNode): DecimalNode {
        return DecimalNode(number.decimalValue().abs())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    @LibraryFunction
    fun floor(number: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#ceil
     */
    @LibraryFunction
    fun ceil(number: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @LibraryFunction
    fun round(number: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @LibraryFunction
    fun round(number: NumericNode, precision: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    @LibraryFunction
    fun power(number: NumericNode, exponent: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    @LibraryFunction
    fun sqrt(number: JsonNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#random
     */
    @LibraryFunction
    fun random() {}

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @LibraryFunction
    fun formatNumber(number: NumericNode, picture: TextNode) {}


    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @LibraryFunction
    fun formatNumber(number: NumericNode, picture: JsonNode, options: ObjectNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#formatbase
     */
    @LibraryFunction
    fun formatBase(number: NumericNode, radix: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#formatinteger
     */
    @LibraryFunction
    fun formatInteger(number: JsonNode, picture: JsonNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#parseinteger
     */
    @LibraryFunction
    fun parseInteger(string: JsonNode, picture: JsonNode) {}

} //~ NumericFunctions