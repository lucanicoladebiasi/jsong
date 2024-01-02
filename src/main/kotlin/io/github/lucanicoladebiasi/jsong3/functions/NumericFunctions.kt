package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import java.math.BigDecimal
import java.math.MathContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.pow
import kotlin.random.Random

@Suppress("unused")
class NumericFunctions(
    private val mc: MathContext,
    private val rand: Random
) {

    companion object {

        private const val TAG_BIN = "0b"
        private const val TAG_HEX = "0x"
        private const val TAG_OCT = "0o"

        const val SYMBOL_DECIMAL_SEPARATOR: String = "decimal-separator"
        const val SYMBOL_GROUPING_SEPARATOR: String = "grouping-separator"
        const val SYMBOL_INFINITY: String = "infinity"
        const val SYMBOL_MINUS_SIGN: String = "minus-sign"
        const val SYMBOL_NAN: String = "NaN"
        const val SYMBOL_PERCENT: String = "percent"
        const val SYMBOL_PER_MILLE: String = "per-mille"
        const val SYMBOL_ZERO_DIGIT: String = "zero-digit"
        const val SYMBOL_DIGIT: String = "digit"
        const val SYMBOL_PATTERN_SEPARATOR: String = "pattern-separator"

        private val DefaultDecimalFormatSymbols: DecimalFormatSymbols = DecimalFormatSymbols(Locale.US)

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
                        txt.startsWith(TAG_BIN) -> txt.substring(TAG_BIN.length).toInt(2).toBigDecimal(mc)
                        txt.startsWith(TAG_OCT) -> txt.substring(TAG_OCT.length).toInt(8).toBigDecimal(mc)
                        txt.startsWith(TAG_HEX) -> txt.substring(TAG_HEX.length).toInt(16).toBigDecimal(mc)
                        else -> txt.toBigDecimal(mc)
                    }
                }

                else -> throw IllegalArgumentException("arg $node is not a number")
            }
        }

        @Throws(IllegalArgumentException::class, NullPointerException::class)
        private fun formatOf(value: BigDecimal, picture: String, symbols: DecimalFormatSymbols): String {
            val df = DecimalFormat()
            df.decimalFormatSymbols = symbols
            val pic: String = picture
                .replace("9".toRegex(), "0")
                .replace("e", "E")
            df.applyLocalizedPattern(pic)
            return df.format(value)
        }

        private fun symbolsOf(options: ObjectNode): DecimalFormatSymbols {
            val symbols = DefaultDecimalFormatSymbols.clone() as DecimalFormatSymbols
            options.fieldNames().forEach { fieldName ->
                val text = options[fieldName].textValue()
                when (fieldName) {
                    SYMBOL_DECIMAL_SEPARATOR -> symbols.decimalSeparator = text[0]
                    SYMBOL_GROUPING_SEPARATOR -> symbols.groupingSeparator = text[0]
                    SYMBOL_INFINITY -> symbols.infinity = text
                    SYMBOL_MINUS_SIGN -> symbols.minusSign = text[0]
                    SYMBOL_NAN -> symbols.naN = text
                    SYMBOL_PERCENT -> symbols.percent = text[0]
                    SYMBOL_PER_MILLE -> symbols.perMill = text[0]
                    SYMBOL_ZERO_DIGIT -> symbols.zeroDigit = text[0]
                    SYMBOL_DIGIT -> symbols.digit = text[0]
                    SYMBOL_PATTERN_SEPARATOR -> symbols.patternSeparator = text[0]
                    else -> throw NoSuchElementException("symbol $fieldName unknown")
                }
            }
            return symbols
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
    fun formatNumber(number: NumericNode, picture: TextNode): TextNode {
        return TextNode(formatOf(number.decimalValue(), picture.textValue(), DefaultDecimalFormatSymbols))
    }


    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @LibraryFunction
    fun formatNumber(number: NumericNode, picture: JsonNode, options: ObjectNode): TextNode {
        return TextNode(formatOf(number.decimalValue(), picture.textValue(), symbolsOf(options)))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatbase
     */
    @LibraryFunction
    fun formatBase(number: NumericNode): TextNode {
        return TextNode(number.intValue().toString())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatbase
     */
    @LibraryFunction
    fun formatBase(number: NumericNode, radix: NumericNode): TextNode {
        return TextNode(Integer.toString(number.intValue(), radix.intValue()))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatinteger
     */
    @LibraryFunction
    fun formatInteger(number: JsonNode, picture: JsonNode) {
    }

    /**
     * https://docs.jsonata.org/numeric-functions#parseinteger
     */
    @LibraryFunction
    fun parseInteger(string: JsonNode, picture: JsonNode) {
    }

} //~ NumericFunctions