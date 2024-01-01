package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.pow
import kotlin.random.Random

@Suppress("unused")
class NumericFunctions(val mc: MathContext, val rand: Random) {

    companion object {

        const val BIN_TAG = "0b"

        const val HEX_TAG = "0x"

        const val OCT_TAG = "0o"

        @Throws(IllegalArgumentException::class)
        fun decimalOf(node: JsonNode?, mc: MathContext): BigDecimal {
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
                        txt.startsWith(BIN_TAG) -> txt.substring(BIN_TAG.length).toInt(2).toBigDecimal(mc)
                        txt.startsWith(OCT_TAG) -> txt.substring(OCT_TAG.length).toInt(8).toBigDecimal(mc)
                        txt.startsWith(HEX_TAG) -> txt.substring(HEX_TAG.length).toInt(16).toBigDecimal(mc)
                        else -> txt.toBigDecimal(mc)
                    }
                }

                else -> throw IllegalArgumentException("arg $node is not a number")
            }
        }

    } //~ companion

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @LibraryFunction
    @Throws(IllegalArgumentException::class)
    fun number(arg: JsonNode): DecimalNode {
        return DecimalNode(decimalOf(arg, mc))
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
    fun floor(number: NumericNode): DecimalNode {
        return DecimalNode(kotlin.math.floor(number.doubleValue()).toBigDecimal(mc))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#ceil
     */
    @LibraryFunction
    fun ceil(number: NumericNode): DecimalNode {
        return DecimalNode(kotlin.math.ceil(number.doubleValue()).toBigDecimal(mc))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @LibraryFunction
    fun round(number: NumericNode): DecimalNode {
        return DecimalNode(number.decimalValue().setScale(0, mc.roundingMode))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @LibraryFunction
    fun round(number: NumericNode, precision: NumericNode): DecimalNode {
        return DecimalNode(number.decimalValue().setScale(precision.intValue(), mc.roundingMode))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    @LibraryFunction
    fun power(number: NumericNode, exponent: NumericNode): DecimalNode {
        return DecimalNode(number.doubleValue().pow(exponent.doubleValue()).toBigDecimal(mc))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    @LibraryFunction
    fun sqrt(number: JsonNode): DecimalNode {
        return DecimalNode(number.decimalValue().sqrt(mc))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#random
     */
    @LibraryFunction
    fun random(): DecimalNode {
        return DecimalNode(rand.nextDouble().toBigDecimal(mc))
    }

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