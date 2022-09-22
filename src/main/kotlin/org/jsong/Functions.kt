package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import java.lang.StringBuilder
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.random.Random

class Functions(
    private val mapper: ObjectMapper,
    private val mathContext: MathContext = MathContext.DECIMAL128
) {

    fun abs(number: JsonNode?): DecimalNode {
        return DecimalNode(
            when (number) {
                null -> throw NullPointerException("<number> null in ${Syntax.ABS}")
                else -> number(number).decimalValue().abs()
            }
        )
    }

    fun add(lhs: JsonNode?, rhs: JsonNode?): DecimalNode {
        return DecimalNode(number(lhs).decimalValue().add(number(rhs).decimalValue()))
    }

    fun and(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(boolean(lhs).booleanValue() && boolean(rhs).booleanValue())
    }

    fun append(array1: JsonNode?, array2: JsonNode?): ArrayNode {
        val exp = mapper.createArrayNode()
        exp.addAll(array(array1))
        exp.addAll(array(array2))
        return exp
    }

    fun array(node: JsonNode?): ArrayNode {
        return when (node) {
            null -> mapper.createArrayNode()
            is ArrayNode -> node
            else -> mapper.createArrayNode().add(node)
        }
    }

    fun average(array: JsonNode?): DecimalNode {
        return when (array) {
            null -> throw NullPointerException("<array> null in ${Syntax.AVERAGE}")
            is ArrayNode -> {
                var sum = BigDecimal.ZERO
                array.forEach { element ->
                    sum = sum.add(number(element).decimalValue())
                }
                div(DecimalNode(sum), DecimalNode(array.size().toBigDecimal()))
            }

            else -> number(array)
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

    fun concatenate(prefix: JsonNode?, suffix: JsonNode?): TextNode {
        return TextNode(StringBuilder(string(prefix).textValue()).append(string(suffix).textValue()).toString())
    }

    fun ceil(number: JsonNode?): DecimalNode {
        return DecimalNode(
            when (number) {
                null -> throw NullPointerException("<number> is null in ${Syntax.CEIL}")
                else -> kotlin.math.ceil(number(number).asDouble()).toBigDecimal()
            }
        )
    }

    fun count(array: JsonNode?): DecimalNode {
        return when (array) {
            null -> throw NullPointerException("<array> is null in ${Syntax.COUNT}")
            is ArrayNode -> DecimalNode(array.size().toBigDecimal())
            else -> DecimalNode(BigDecimal.ONE)
        }
    }

    fun distinct(array: JsonNode?): ArrayNode {
        val exp = mapper.nodeFactory.arrayNode()
        when (array) {
            null -> throw NullPointerException("<array> null in ${Syntax.DISTINCT}")
            is ArrayNode -> array.forEach { if (!exp.contains(it)) exp.add(it) }
            else -> exp.add(array)
        }
        return exp
    }

    fun div(lhs: JsonNode?, rhs: JsonNode?): DecimalNode {
        val dividend = number(lhs).decimalValue()
        val divisor = number(rhs).decimalValue()
        return DecimalNode(dividend.divide(divisor, mathContext))
    }

    fun eq(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(flatten(lhs) == flatten(rhs))
    }

    fun exists(arg: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(
            when (arg) {
                is ArrayNode -> !arg.isEmpty
                else -> arg != null
            }
        )
    }

    fun flatten(node: JsonNode?): JsonNode? {
        return when (node) {
            is RangesNode -> when (node.size()) {
                0 -> null
                1 -> flatten(node[0]) as RangeNode
                else -> {
                    val res = RangesNode(mapper.nodeFactory)
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

    fun floor(number: JsonNode?): DecimalNode {
        return DecimalNode(
            when (number) {
                null -> throw java.lang.NullPointerException("<number> is null in ${Syntax.FLOOR}")
                else -> kotlin.math.floor(number(number).asDouble()).toBigDecimal()
            }
        )
    }

    fun formatBase(number: JsonNode?, radix: JsonNode? = null): TextNode {
        val base = radix?.asInt(10) ?: 10
        when {
            base < 2 -> throw IllegalArgumentException("<radix> < 2 in ${Syntax.FORMAT_BASE}")
            base > 36 -> throw IllegalArgumentException("<radix> > 36 in ${Syntax.FORMAT_BASE}")
            else -> return TextNode(
                when (number) {
                    null -> throw IllegalArgumentException("<number> is null in ${Syntax.FORMAT_BASE}")
                    else -> number(number).asInt().toString(radix?.asInt(10) ?: 10)
                }
            )
        }
    }

    @Throws(UnsupportedOperationException::class)
    @Suppress("UNUSED_PARAMETER")
    fun formatInteger(number: JsonNode?, picture: JsonNode?): TextNode {
        throw UnsupportedOperationException("Not implemented yet")
    }

    @Throws(UnsupportedOperationException::class)
    @Suppress("UNUSED_PARAMETER")
    fun formatNumber(number: JsonNode?, picture: JsonNode?, options: JsonNode? = null): TextNode {
        throw UnsupportedOperationException("Not implemented yet")
    }

    fun gt(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(
            when (val lhn = flatten(lhs)) {
                null -> false
                else -> when (val rhn = flatten(rhs)) {
                    null -> false
                    is DecimalNode -> lhn.decimalValue() > rhn.decimalValue()
                    is NumericNode -> lhn.decimalValue() > rhn.decimalValue()
                    else -> lhn.asText() > rhn.asText()
                }
            }
        )
    }

    fun gte(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(
            when (val lhn = flatten(lhs)) {
                null -> false
                else -> when (val rhn = flatten(rhs)) {
                    null -> false
                    is DecimalNode -> lhn.decimalValue() >= rhn.decimalValue()
                    is NumericNode -> lhn.decimalValue() >= rhn.decimalValue()
                    else -> lhn.asText() > rhn.asText()
                }
            }
        )
    }

    fun include(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(array(flatten(rhs)).contains(flatten(lhs)))
    }

    fun lt(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(
            when (val lhn = flatten(lhs)) {
                null -> false
                else -> when (val rhn = flatten(rhs)) {
                    null -> false
                    is DecimalNode -> lhn.decimalValue() < rhn.decimalValue()
                    is NumericNode -> lhn.decimalValue() < rhn.decimalValue()
                    else -> lhn.asText() > rhn.asText()
                }
            }
        )
    }

    fun lte(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(
            when (val lhn = flatten(lhs)) {
                null -> false
                else -> when (val rhn = flatten(rhs)) {
                    null -> false
                    is DecimalNode -> lhn.decimalValue() <= rhn.decimalValue()
                    is NumericNode -> lhn.decimalValue() <= rhn.decimalValue()
                    else -> lhn.asText() > rhn.asText()
                }
            }
        )
    }

    fun ne(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(flatten(lhs) != flatten(rhs))
    }

    fun max(array: JsonNode?): DecimalNode {
        return when (array) {
            null -> throw NullPointerException("<array> is null in ${Syntax.MAX}")
            is ArrayNode -> {
                var max = Double.MIN_VALUE
                array.forEach { element ->
                    max = max.coerceAtLeast(number(element).asDouble())
                }
                DecimalNode(max.toBigDecimal())
            }

            else -> number(array)
        }
    }

    fun min(array: JsonNode?): DecimalNode {
        return when (array) {
            null -> throw NullPointerException("<array> is null in ${Syntax.MIN}")
            is ArrayNode -> {
                var min = Double.MAX_VALUE
                array.forEach { element -> min = min.coerceAtMost(element.asDouble()) }
                DecimalNode(min.toBigDecimal())
            }

            else -> number(array)
        }
    }

    fun mul(lhs: JsonNode?, rhs: JsonNode?): DecimalNode {
        return DecimalNode(
            number(lhs).decimalValue().multiply(number(rhs).decimalValue())
        )
    }

    fun not(arg: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(!boolean(arg).booleanValue())
    }

    fun number(arg: JsonNode?): DecimalNode {
        return DecimalNode(
            when (val scalar = flatten(arg)) {
                null -> throw NullPointerException("<arg> null in ${Syntax.NUMBER}")
                is ArrayNode -> throw IllegalArgumentException("<arg> is array, can't cast as number in ${Syntax.NUMBER}")
                is BooleanNode -> when (scalar) {
                    BooleanNode.TRUE -> BigDecimal.ONE
                    else -> BigDecimal.ZERO
                }

                is RangeNode -> throw IllegalArgumentException("<arg> is range, can't cast as number in ${Syntax.NUMBER}")
                is NumericNode -> scalar.decimalValue()
                else -> scalar.asText().toBigDecimal()

            }
        )
    }

    fun or(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(boolean(lhs).booleanValue() || boolean(rhs).booleanValue())
    }

    @Throws(UnsupportedOperationException::class)
    @Suppress("UNUSED_PARAMETER")
    fun parseInteger(string: JsonNode?, picture: JsonNode?): DecimalNode {
        throw UnsupportedOperationException("Not implemented yet")
    }

    fun power(base: JsonNode?, exponent: JsonNode?): DecimalNode {
        return DecimalNode(
            when (base) {
                null -> throw IllegalArgumentException("<base> is null in ${Syntax.POWER}")
                else -> when (exponent) {
                    null -> throw IllegalArgumentException("<exponent> is null in ${Syntax.POWER}")
                    else -> number(base).decimalValue().pow(number(exponent).asInt())
                }
            }
        )
    }

    fun randomFrom(random: Random): DecimalNode {
        return DecimalNode(random.nextDouble().toBigDecimal())
    }

    fun reminder(lhs: JsonNode?, rhs: JsonNode?): DecimalNode {
        return DecimalNode(number(lhs).decimalValue().remainder(number(rhs).decimalValue()))
    }

    fun reverse(array: JsonNode?): ArrayNode {
        val list = mutableListOf<JsonNode>()
        when (array) {
            null -> throw NullPointerException("<array> is null in ${Syntax.REVERSE}")
            is ArrayNode -> array.forEach { list.add(it) }
            else -> list.add(array)
        }
        list.reverse()
        return mapper.createArrayNode().addAll(list)
    }

    fun round(number: JsonNode?, precision: JsonNode? = null): DecimalNode {
        return DecimalNode(
            when (number) {
                null -> throw NullPointerException("<number> is null in ${Syntax.ROUND}")
                else -> {
                    val scale = precision?.let { number(it).asInt() } ?: 0
                    number(number).decimalValue().setScale(scale, RoundingMode.HALF_EVEN)
                }
            }
        )
    }

    @Throws(UnsupportedOperationException::class)
    @Suppress("UNUSED_PARAMETER")
    fun sort(array: JsonNode?): ArrayNode {
        throw UnsupportedOperationException("Not implemented yet")
    }

    fun shuffle(array: JsonNode?, random: Random): ArrayNode {
        val list = mutableListOf<JsonNode>()
        when (array) {
            null -> throw NullPointerException("<array> null in ${Syntax.SHUFFLE}")
            is RangeNode -> array.indexes.forEach { list.add(it) }
            is RangesNode -> array.indexes.forEach { list.add(it) }
            is ArrayNode -> array.forEach { list.add(it) }
            else -> list.add(array)
        }
        return mapper.createArrayNode().addAll(list.shuffled(random))
    }

    fun sqrt(number: JsonNode?): DecimalNode {
        return DecimalNode(
            when (number) {
                null -> throw NullPointerException("<number> is null in ${Syntax.SQRT}")
                else -> kotlin.math.sqrt(number(number).asDouble()).toBigDecimal()
            }
        )
    }

    fun string(arg: JsonNode?, prettify: JsonNode? = null): TextNode {
        return when (arg) {
            is TextNode -> arg
            else -> TextNode(
                when (prettify?.asBoolean() ?: false) {
                    true -> mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arg)
                    else -> mapper.writeValueAsString(arg)
                }
            )
        }
    }

    fun sub(lhs: JsonNode?, rhs: JsonNode?): DecimalNode {
        return DecimalNode(number(lhs).decimalValue().subtract(number(rhs).decimalValue()))
    }

    fun sum(array: JsonNode?): DecimalNode {
        return when (array) {
            null -> throw NullPointerException("<array> is null in ${Syntax.SUM}")
            is ArrayNode -> {
                var sum = BigDecimal.ZERO
                array.forEach { element ->
                    sum = sum.add(number(element).decimalValue())
                }
                DecimalNode(sum)
            }

            else -> number(array)
        }
    }

}