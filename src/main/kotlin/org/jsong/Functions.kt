package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import java.lang.StringBuilder
import java.math.BigDecimal
import java.math.MathContext
import kotlin.random.Random

class Functions(
    private val mapper: ObjectMapper,
    private val mathContext: MathContext = MathContext.DECIMAL128
) {

    fun add(lhs: JsonNode?, rhs: JsonNode?): DecimalNode {
        return DecimalNode((lhs?.decimalValue() ?: BigDecimal.ZERO).add(rhs?.decimalValue() ?: BigDecimal.ZERO))
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
        val dividend = lhs?.decimalValue() ?: BigDecimal.ZERO
        val divisor = rhs?.decimalValue() ?: BigDecimal.ZERO
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
            (flatten(lhs)?.decimalValue() ?: BigDecimal.ZERO)
                .multiply(flatten(rhs)?.decimalValue() ?: BigDecimal.ZERO)
        )
    }

    fun not(arg: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(!boolean(arg).booleanValue())
    }

    fun number(arg: JsonNode?): DecimalNode {
        return DecimalNode(
            when (arg) {
                null -> throw NullPointerException("<arg> null in ${Syntax.NUMBER}")
                is BooleanNode -> when (arg) {
                    BooleanNode.TRUE -> BigDecimal.ONE
                    else -> BigDecimal.ZERO
                }

                is NumericNode -> arg.decimalValue()
                else -> arg.asText().toBigDecimal()
            }
        )
    }

    fun or(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(boolean(lhs).booleanValue() || boolean(rhs).booleanValue())
    }

    fun reminder(lhs: JsonNode?, rhs: JsonNode?): DecimalNode {
        return DecimalNode((lhs?.decimalValue() ?: BigDecimal.ZERO).remainder(rhs?.decimalValue() ?: BigDecimal.ZERO))
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

    fun sort(array: JsonNode?): ArrayNode {
        TODO("not implemented yet")
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
        return DecimalNode((lhs?.decimalValue() ?: BigDecimal.ZERO).subtract(rhs?.decimalValue() ?: BigDecimal.ZERO))
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