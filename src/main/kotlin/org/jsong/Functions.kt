package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import java.lang.StringBuilder
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

class Functions(
    private val mapper: ObjectMapper,
    private val random: Random,
    private val time: Instant,
    private val mathContext: MathContext = MathContext.DECIMAL128,
) {

    companion object {

        /**
         * Used in [match] method..
         */
        private const val TAG_GROUPS = "groups"

        /**
         * Used in [match] method.
         */
        private const val TAG_INDEX = "index"

        /**
         * Used in [match] method.
         */
        private const val TAG_MATCH = "match"

        /**
         * Used in [trim] method.
         */
        private val whitespaceRegex = "\\s+".toRegex()

    }

    enum class Type(val descriptor: TextNode) {
        ARRAY(TextNode("array")),
        BOOLEAN(TextNode("boolean")),
        NULL(TextNode("null")),
        NUMBER(TextNode("number")),
        OBJECT(TextNode("object")),
        STRING(TextNode("string")),
        UNDEFINED(TextNode("undefined"))
    }

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

    fun assert(condition: JsonNode?, message: JsonNode?): BooleanNode {
        return when (boolean(condition)) {
            BooleanNode.TRUE -> BooleanNode.TRUE
            else -> throw AssertionError(string(message).toString())
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

    fun base64decode(str: JsonNode?): TextNode {
        return when (str) {
            null -> throw java.lang.NullPointerException("<str> null in ${Syntax.BASE64_DECODE}")
            else -> TextNode(Base64.getDecoder().decode(string(str).asText()).toString(Charsets.UTF_8))
        }
    }

    fun base64encode(str: JsonNode?): TextNode {
        return when (val flt = flatten(str)) {
            null -> throw NullPointerException("<str> null in ${Syntax.BASE64_ENCODE}")
            else -> TextNode(Base64.getEncoder().encodeToString(flt.asText().toByteArray()))
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

    fun contains(str: JsonNode?, pattern: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(
            when (val flt = flatten(str)) {
                null -> throw NullPointerException("<str> null in ${Syntax.CONTAINS}")
                else -> when (pattern) {
                    null -> throw NullPointerException("<pattern> null in ${Syntax.CONTAINS}")
                    is RegexNode -> flt.asText().contains(pattern.regex)
                    else -> flt.asText().contains(string(pattern).asText())
                }
            }
        )
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

    fun decodeUrl(str: JsonNode?): TextNode {
        return when (val flt = flatten(str)) {
            null -> throw java.lang.NullPointerException("<str> is null in ${Syntax.DECODE_URL}")
            else -> TextNode(URLDecoder.decode(flt.asText(), Charsets.UTF_8.toString()))
        }
    }

    fun decodeUrlComponent(str: JsonNode?): JsonNode {
        return when (val flt = flatten(str)) {
            null -> throw IllegalArgumentException("str is null in ${Syntax.DECODE_URL_COMPONENT}")
            else -> TextNode(URLDecoder.decode(flt.asText(), Charsets.UTF_8.toString()))
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

    fun encodeUrl(str: JsonNode?): TextNode {
        return when (val flt = flatten(str)) {
            null -> throw NullPointerException("<str> null in ${Syntax.ENCODE_URL}")
            else -> TextNode(URLEncoder.encode(flt.asText(), Charsets.UTF_8.toString()))
        }
    }

    fun encodeUrlComponent(str: JsonNode?): TextNode {
        return when (val flt = flatten(str)) {
            null -> throw NullPointerException("<str> is null in ${Syntax.ENCODE_URL_COMPONENT}")
            else -> TextNode(URLEncoder.encode(flt.asText(), Charsets.UTF_8.toString()))
        }
    }

    fun eq(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(flatten(lhs) == flatten(rhs))
    }

    fun error(message: JsonNode?): JsonNode? {
        @Suppress("UNREACHABLE_CODE")
        return when (message) {
            null -> throw java.lang.IllegalArgumentException("<message> is null in ${Syntax.ERROR}")
            else -> throw Error(string(message).asText())
        }
    }

    fun eval(expr: JsonNode?, context: JsonNode? = null): JsonNode? {
        return when (val exp = flatten(expr)) {
            null -> expr
            else -> JSong.of(exp.asText(), mapper, random, time).evaluate(context)
        }
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

    fun fromMillis(number: JsonNode?, picture: JsonNode? = null, timezone: JsonNode? = null): String {
        return when (val value = flatten(number)) {
            null -> throw IllegalArgumentException("number is null in ${Syntax.FROM_MILLIS}")
            else -> {
                val dtf = (picture
                    ?.let { DateTimeFormatter.ofPattern(picture.asText()) }
                    ?: DateTimeFormatter.ISO_INSTANT)
                    .withZone(timezone
                        ?.let { ZoneId.of(it.asText()) }
                        ?: ZoneId.systemDefault()
                    )
                dtf.format(Instant.ofEpochMilli(value.longValue()))
            }
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
        val container = array(flatten(rhs))
        array(flatten(lhs)).forEach {
            if (container.contains(it)) {
                return BooleanNode.TRUE
            }
        }
        return BooleanNode.FALSE
    }

    fun join(array: JsonNode?, separator: JsonNode? = null): TextNode {
        return when (array) {
            null -> throw NullPointerException("<array> is null in ${Syntax.JOIN}")
            !is ArrayNode -> TextNode(string(array).asText())
            else -> TextNode(array.joinToString(separator?.asText("") ?: "") { string(it).asText() })
        }
    }

    fun keys(obj: JsonNode?): ArrayNode {
        val set = mutableSetOf<String>()
        when (obj) {
            is ArrayNode -> obj.forEach {
                if (it is ObjectNode) it.fieldNames().forEach { key -> set.add(key) }
            }

            is ObjectNode -> obj.fieldNames().forEach { key -> set.add(key) }
            else -> {} // do nothing
        }
        return mapper.createArrayNode().addAll(set.map { TextNode(it) })
    }

    fun length(str: JsonNode?): DecimalNode {
        return when (val flt = flatten(str)) {
            null -> throw NullPointerException("<str> is null in ${Syntax.LENGTH_OF}")
            else -> DecimalNode(flt.asText().length.toBigDecimal())
        }
    }

    fun lookup(obj: JsonNode?, key: JsonNode?): JsonNode? {
        return when (key) {
            null -> null
            else -> when (obj) {
                is ArrayNode -> mapper.createArrayNode()
                    .addAll(obj.filterIsInstance<ObjectNode>().map { it[key.asText()] })

                is ObjectNode -> obj[key.asText()]
                else -> null
            }
        }
    }

    fun lowercase(str: JsonNode?): TextNode {
        return when (val flt = flatten(str)) {
            null -> throw NullPointerException("<str> is null in ${Syntax.LOWERCASE}")
            else -> TextNode(flt.asText().lowercase((Locale.getDefault())))
        }
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

    @Suppress("UNUSED_PARAMETER")
    fun match(str: JsonNode?, pattern: JsonNode?, limit: JsonNode? = null): ArrayNode {
        return when (str) {
            null -> throw NullPointerException("<str> null in ${Syntax.MATCH}")
            else -> when (pattern) {
                !is RegexNode -> throw NullPointerException("<pattern> is not regex in ${Syntax.MATCH}")
                else -> mapper.nodeFactory.arrayNode().addAll(
                    pattern.regex.findAll(string(str).asText()).map { matchResult ->
                        mapper.nodeFactory.objectNode()
                            .put(TAG_MATCH, matchResult.value)
                            .put(TAG_INDEX, matchResult.range.first)
                            .set<ObjectNode>(
                                TAG_GROUPS,
                                mapper.nodeFactory.arrayNode().addAll(matchResult.groupValues.map { TextNode(it) })
                            )
                    }.toList()
                )
            }
        }
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

    fun merge(array: JsonNode?): ObjectNode {
        val obj = mapper.createObjectNode()
        if (array is ArrayNode) {
            array.filterIsInstance<ObjectNode>().forEach { node ->
                node.fields().forEach { entry ->
                    when (obj.has(entry.key)) {
                        true -> {
                            val arr = mapper.createArrayNode()
                            when (obj[entry.key]) {
                                null -> {}
                                is ArrayNode -> obj[entry.key].forEach { arr.add(it) }
                                else -> arr.add(obj[entry.key])
                            }
                            arr.add(entry.value)
                            obj.set<ArrayNode>(entry.key, arr)
                        }

                        else -> obj.set<JsonNode>(entry.key, entry.value)
                    }
                }
            }
        }
        return obj
    }

    fun millis(time: Instant): DecimalNode {
        return DecimalNode(time.toEpochMilli().toBigDecimal())
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

    fun ne(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(flatten(lhs) != flatten(rhs))
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

    fun now(time: Instant, picture: JsonNode? = null, timezone: JsonNode? = null): TextNode {
        val dtf = (
                flatten(picture)
                    ?.let { DateTimeFormatter.ofPattern(picture?.asText()) }
                    ?: DateTimeFormatter.ISO_INSTANT
                )
            .withZone(
                flatten(timezone)
                    ?.let { ZoneId.of(it.asText()) }
                    ?: ZoneId.systemDefault()
            )
        return TextNode(dtf.format(time))
    }

    fun or(lhs: JsonNode?, rhs: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(boolean(lhs).booleanValue() || boolean(rhs).booleanValue())
    }

    fun pad(str: JsonNode?, width: JsonNode?, char: JsonNode? = null): TextNode {
        return when (val flt = flatten(str)) {
            null -> throw NullPointerException("<str> is null in ${Syntax.PAD}")
            else -> when (width) {
                null -> throw NullPointerException("<width> is null in ${Syntax.PAD}")
                else -> {
                    val txt = flt.asText()
                    val offset = width.asInt(0)
                    val pad = char?.asText()?.get(0) ?: ' '
                    TextNode(
                        when {
                            offset < 0 -> txt.padStart(-offset, pad)
                            else -> txt.padEnd(offset, pad)
                        }
                    )
                }
            }
        }
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

    fun replace(str: JsonNode?, pattern: JsonNode?, replacement: JsonNode?): TextNode {
        return when (val flt = flatten(str)) {
            null -> throw NullPointerException("<str> is null in ${Syntax.REPLACE}")
            else -> when (replacement) {
                null -> throw NullPointerException("<replacement> is null in ${Syntax.REPLACE}")
                else -> when (pattern) {
                    is RegexNode -> TextNode(flt.asText().replace(pattern.regex, string(replacement).asText()))
                    else -> TextNode(
                        flt.asText().replace(string(pattern).asText(), string(replacement).asText())
                    )
                }
            }
        }
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
            is ArrayNode -> array.forEach {
                when (it) {
                    is RangeNode -> list.addAll(it.indexes)
                    is RangesNode -> list.addAll(it.indexes)
                    else -> list.add(it)
                }
            }
            else -> list.add(array)
        }
        return mapper.createArrayNode().addAll(list.shuffled(random))
    }

    fun split(str: JsonNode?, separator: JsonNode?, limit: JsonNode? = null): ArrayNode {
        return mapper.nodeFactory.arrayNode().addAll(
            when (val flt_str = flatten(str)) {
                null -> throw NullPointerException("<str> is null in ${Syntax.SPLIT}")
                else -> when (separator) {
                    null -> throw NullPointerException("<separator> is null in ${Syntax.SPLIT}")
                    is RegexNode -> flt_str.asText().split(separator.regex, limit?.asInt(0) ?: 0)
                    else -> flt_str.asText().split(
                        separator.asText(),
                        ignoreCase = false,
                        limit = limit?.asInt(0) ?: 0
                    )
                }
            }.map { TextNode(it) }
        )
    }

    fun spread(obj: JsonNode?): ArrayNode {
        val res = mapper.createArrayNode()
        when (obj) {
            is ArrayNode -> {
                obj.filterIsInstance<ObjectNode>().forEach {
                    it.fields().forEach { entry ->
                        res.add(mapper.createObjectNode().set<JsonNode>(entry.key, entry.value))
                    }
                }
            }

            is ObjectNode -> {
                obj.fields().forEach { entry ->
                    res.add(mapper.createObjectNode().set<JsonNode>(entry.key, entry.value))
                }
            }
        }
        return res
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
                    true -> mapper.writerWithDefaultPrettyPrinter().writeValueAsString(flatten(arg))
                    else -> mapper.writeValueAsString(flatten(arg))
                }
            )
        }
    }

    fun sub(lhs: JsonNode?, rhs: JsonNode?): DecimalNode {
        return DecimalNode(number(lhs).decimalValue().subtract(number(rhs).decimalValue()))
    }

    fun substring(str: JsonNode?, start: JsonNode? = null, length: JsonNode? = null): TextNode {
        return when (val flt = flatten(str)) {
            null -> throw NullPointerException("<str> is null in ${Syntax.SUBSTRING}")
            else -> {
                val txt = flt.asText()
                val offset = start?.asInt() ?: 0
                val first = 0.coerceAtLeast(if (offset < 0) txt.length + offset else offset)
                    .coerceAtMost(txt.length)
                val last = 0.coerceAtLeast(length?.let { first + length.asInt() } ?: txt.length)
                    .coerceAtMost(txt.length)
                TextNode(txt.substring(first, last))
            }
        }
    }

    fun substringAfter(str: JsonNode?, chars: JsonNode?): TextNode {
        return TextNode(
            when (val flt = flatten(str)) {
                null -> throw NullPointerException("<str> is null in ${Syntax.SUBSTRING_AFTER}")
                else -> when (chars) {
                    null -> throw NullPointerException("<chars> is null in ${Syntax.SUBSTRING_AFTER}")
                    else -> flt.asText().substringAfter(string(chars).asText())
                }
            }
        )
    }

    fun substringBefore(str: JsonNode?, chars: JsonNode?): TextNode {
        return TextNode(
            when (val flt = flatten(str)) {
                null -> throw NullPointerException("<str> is null in ${Syntax.SUBSTRING_BEFORE} ")
                else -> when (chars) {
                    null -> throw NullPointerException("<chars> is null in ${Syntax.SUBSTRING_BEFORE}")
                    else -> flt.asText().substringBefore(string(chars).asText())
                }
            }
        )
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

    fun toMillis(timestamp: JsonNode?, picture: JsonNode? = null): DecimalNode {
        val time = flatten(timestamp)
        val form = flatten(picture)
        return when (time) {
            null -> throw IllegalArgumentException("timestamp is null in ${Syntax.TO_MILLIS}")
            else -> {
                val dtf = (form
                    ?.let { DateTimeFormatter.ofPattern(form.asText()) }
                    ?: DateTimeFormatter.ISO_INSTANT)
                    .withZone(ZoneId.systemDefault())
                DecimalNode(Instant.from(dtf.parse(time.asText())).toEpochMilli().toBigDecimal())
            }
        }
    }

    fun trim(str: JsonNode?): TextNode {
        return TextNode(
            when (val flt = flatten(str)) {
                null -> throw NullPointerException("<str> null in ${Syntax.TRIM}")
                else -> flt.asText().replace(whitespaceRegex, " ").trim()
            }
        )
    }

    fun type(value: JsonNode?): TextNode {
        return when (value) {
            is ArrayNode -> Type.ARRAY.descriptor
            is BooleanNode -> Type.BOOLEAN.descriptor
            is NullNode -> Type.NULL.descriptor
            is NumericNode -> Type.NUMBER.descriptor
            is ObjectNode -> Type.OBJECT.descriptor
            is TextNode -> Type.STRING.descriptor
            else -> Type.UNDEFINED.descriptor
        }
    }

    fun uppercase(str: JsonNode?): TextNode {
        return TextNode(
            when (val flt = flatten(str)) {
                null -> throw NullPointerException("<str> is null in ${Syntax.UPPERCASE}")
                else -> flt.asText().uppercase(Locale.getDefault())
            }
        )
    }

}