package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.random.Random

/**
 * https://docs.jsonata.org/numeric-functions
 */
@Suppress("FunctionName", "unused")
class NumericFunctions(
    private val mathContext: MathContext,
    private val random: Random
) {

    companion object {

        const val BIN_TAG = "0b"

        const val HEX_TAG = "0x"

        const val OCT_TAG = "0o"

        fun decimal(numeric: NumericNode): BigDecimal {
            return numeric.asText().toBigDecimal()
        }

        fun format(mathContext: MathContext, number: Double, picture: String): String {
            //val symbols = DecimalFormatSymbols(Locale.US)
            val formatter = DecimalFormat()
            formatter.roundingMode = mathContext.roundingMode
            //formatter.decimalFormatSymbols = symbols
            val fixedPicture = picture.replace("9", "0").replace("e", "E") // e is not
            formatter.applyLocalizedPattern(fixedPicture)
            return formatter.format(number)
        }

    } //~ companion

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Throws(NumberFormatException::class)
    fun `$number`(arg: JsonNode): NumericNode {
        return when (arg) {
            is BooleanNode -> when (arg.booleanValue()) {
                true -> DecimalNode(BigDecimal.ONE)
                else -> DecimalNode(BigDecimal.ZERO)
            }

            is NumericNode -> DecimalNode(decimal(arg))
            is TextNode -> {
                val exp = arg.textValue()
                return DecimalNode(
                    when {
                        exp.startsWith(BIN_TAG) -> exp.substring(BIN_TAG.length).toBigInteger(2).toBigDecimal()
                        exp.startsWith(OCT_TAG) -> exp.substring(OCT_TAG.length).toBigInteger(8).toBigDecimal()
                        exp.startsWith(HEX_TAG) -> exp.substring(HEX_TAG.length).toBigInteger(16).toBigDecimal()
                        else -> exp.toBigDecimal()
                    }
                )
            }

            else -> throw IllegalArgumentException("$arg can't be cast to a number")
        }
    }

    /**
     * https://docs.jsonata.org/numeric-functions#abs
     */
    fun `$abs`(number: NumericNode): NumericNode {
        return DecimalNode(decimal(number).abs())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    fun `$floor`(number: NumericNode): NumericNode {
        return DecimalNode(floor(decimal(number).toDouble()).toBigDecimal())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    fun `$ceil`(number: NumericNode): DecimalNode {
        return DecimalNode(ceil(decimal(number).toDouble()).toBigDecimal())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    fun `$round`(number: NumericNode): NumericNode {
        return DecimalNode(decimal(number).setScale(0, RoundingMode.HALF_EVEN))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    fun `$round`(number: NumericNode, precision: NumericNode): NumericNode {
        return DecimalNode(decimal(number).setScale(precision.asInt(), RoundingMode.HALF_EVEN))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    fun `$power`(base: NumericNode, exponent: NumericNode): NumericNode {
        return DecimalNode(decimal(base).toDouble().pow(decimal(exponent).toDouble()).toBigDecimal())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    fun `$random`(): NumericNode {
        return DecimalNode(random.nextDouble().toBigDecimal())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    fun `$formatNumber`(number: NumericNode, picture: TextNode): TextNode {
        return TextNode(format(mathContext, number.asDouble(), picture.textValue()))
    }

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