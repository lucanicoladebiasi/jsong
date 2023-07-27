package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.math.BigDecimal
import kotlin.jvm.Throws

/**
 * https://docs.jsonata.org/numeric-functions
 */
@Suppress("FunctionName", "unused")
class NumericFunctions {

    companion object {

        const val BIN_TAG = "0b"

        const val HEX_TAG = "0x"

        const val OCT_TAG = "0o"
    }

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Throws(NumberFormatException::class)
    fun `$number`(arg: JsonNode): NumericNode {
        return when(arg) {
            is BooleanNode -> when(arg.booleanValue()) {
                true -> DecimalNode(BigDecimal.ONE)
                else -> DecimalNode(BigDecimal.ZERO)
            }
            is NumericNode -> DecimalNode(arg.asText().toBigDecimal())
            is TextNode -> {
                val exp = arg.textValue()
                return DecimalNode(when {
                    exp.startsWith(BIN_TAG) -> exp.substring(BIN_TAG.length).toBigInteger(2).toBigDecimal()
                    exp.startsWith(OCT_TAG) -> exp.substring(OCT_TAG.length).toBigInteger(8).toBigDecimal()
                    exp.startsWith(HEX_TAG) -> exp.substring(HEX_TAG.length).toBigInteger(16).toBigDecimal()
                    else -> exp.toBigDecimal()
                })
            }
            else -> throw IllegalArgumentException("$arg can't be cast to a number")
        }
    }

    /**
     * https://docs.jsonata.org/numeric-functions#abs
     */
    fun `$abs`(number: NumericNode): NumericNode {
        return DecimalNode(number.asText().toBigDecimal().abs())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    fun `$ceil`(number: NumericNode){}

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    fun `$round`(number: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    fun `$round`(number: NumericNode, precision:NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    fun `$power`(base: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    fun `$power`(base: NumericNode, exponent: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    fun `$random`() {}

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    fun `$formatNumber`(number: NumericNode, picture: TextNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    fun `$formatNumber`(number: NumericNode, picture: TextNode, options: ObjectNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#formatbase
     */
    fun `$formatBase`(number: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#formatbase
     */
    fun `$formatBase`(number: NumericNode, radix: NumericNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#formatinteger
     */
    fun `$formatInteger`(number: NumericNode, picture: TextNode) {}

    /**
     * https://docs.jsonata.org/numeric-functions#parseinteger
     */
    fun `$parseInteger`(string: TextNode, picture: TextNode) {}

} //~ NumericFunctions