package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import java.lang.Integer.min
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class Library(
    private val processor: Processor
) : JSonataLFunctions {

    companion object {

        const val IS_ARRAY = "array"
        const val IS_BOOLEAN = "boolean"
        const val IS_FUNCTION = "function"
        const val IS_NULL = "null"
        const val IS_NUMBER = "number"
        const val IS_OBJECT = "object"
        const val IS_STRING = "string"
        const val IS_UNDEFINED = "undefined"


        /**
         * Used in [match] method..
         */
        const val TAG_GROUPS = "groups"

        /**
         * Used in [match] method.
         */
        const val TAG_INDEX = "index"

        /**
         * Used in [match] method.
         */
        const val TAG_MATCH = "match"

        /**
         * Used in [trim] method.
         */
        private val whitespaceRegex = "\\s+".toRegex()

    }

    override fun abs(number: DecimalNode): DecimalNode {
        return DecimalNode(number.decimalValue().abs())
    }

    override fun append(array1: JsonNode, array2: JsonNode): ArrayNode {
        return processor.nf.arrayNode()
            .addAll(processor.expand(array1))
            .addAll(processor.expand(array2))
    }

    override fun assert(condition: JsonNode, message: JsonNode) {
        if (!boolean(condition).booleanValue()) {
            throw AssertionError(message.textValue())
        }
    }

    override fun average(array: ArrayNode): DecimalNode {
        return DecimalNode(sum(array).decimalValue().divide(array.size().toBigDecimal()))
    }

    override fun base64decode(str: TextNode): TextNode {
        return TextNode(Base64.getDecoder().decode(str.textValue()).toString())
    }

    override fun base64encode(str: TextNode): TextNode {
        return TextNode(Base64.getEncoder().encodeToString(str.textValue().toByteArray()))
    }

    override fun boolean(arg: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(when (arg) {
            is ArrayNode -> {
                arg.forEach { node ->
                    if (boolean(node).booleanValue()) {
                        return BooleanNode.TRUE
                    }
                }
                false
            }

            is BooleanNode -> arg.booleanValue()
            is NumericNode -> arg.decimalValue() != BigDecimal.ZERO
            is ObjectNode -> !arg.isEmpty
            is TextNode -> arg.textValue().isNotBlank()
            else -> false
        })
    }

    override fun ceil(number: DecimalNode): DecimalNode {
        return DecimalNode(kotlin.math.ceil(number.asDouble()).toBigDecimal())
    }

    override fun contains(str: TextNode, pattern: RegexNode): BooleanNode {
        return BooleanNode.valueOf(str.textValue().contains(pattern.regex))
    }

    override fun contains(str: TextNode, pattern: TextNode): BooleanNode {
        return BooleanNode.valueOf(str.textValue().contains(pattern.textValue()))
    }

    override fun count(array: JsonNode): DecimalNode {
        return DecimalNode(
            when (array) {
                is ArrayNode -> array.size().toBigDecimal()
                else -> BigDecimal.ONE
            }
        )
    }

    override fun decodeUrl(str: TextNode): TextNode {
        return TextNode(URLDecoder.decode(str.textValue(), Charsets.UTF_8.toString()))
    }

    override fun decodeUrlComponent(str: TextNode): TextNode {
        return TextNode(URLDecoder.decode(str.textValue(), Charsets.UTF_8.toString()))
    }

    override fun distinct(array: ArrayNode): ArrayNode {
        return processor.nf.arrayNode().addAll(array.toSet())
    }

    override fun each(obj: ObjectNode, function: FunNode): ArrayNode {
        TODO("Not yet implemented")
    }

    override fun error(message: JsonNode) {
        throw Error(message.textValue())
    }

    override fun exists(arg: TextNode): BooleanNode {
        val exp = arg.textValue()
        val res = Processor(processor.root).evaluate(exp)
        return BooleanNode.valueOf(res != null)
    }

    override fun encodeUrl(str: TextNode): TextNode {
        return TextNode(URLEncoder.encode(str.textValue(), Charsets.UTF_8.toString()))
    }

    override fun encodeUrlComponent(str: TextNode): TextNode {
        return TextNode(URLEncoder.encode(str.textValue(), Charsets.UTF_8.toString()))
    }

    override fun eval(expr: TextNode, context: JsonNode?): JsonNode? {
        return Processor(context).evaluate(expr.textValue())
    }

    override fun filter(array: ArrayNode, function: FunNode): ArrayNode {
        TODO("Not yet implemented")
    }

    override fun floor(number: DecimalNode): DecimalNode {
        return DecimalNode(kotlin.math.floor(number.asDouble()).toBigDecimal())
    }

    override fun formatBase(number: DecimalNode, radix: DecimalNode?): TextNode {
        val base = radix?.asInt() ?: 10
        return TextNode(
            when {
                base < 2 -> throw IllegalArgumentException("<radix> < 2")
                base > 36 -> throw IllegalArgumentException("<radix> > 36")
                else -> number.asInt().toString(base)
            }
        )
    }

    override fun formatInteger(number: DecimalNode, picture: TextNode): TextNode {
        TODO("Not yet implemented")
    }

    override fun formatNumber(number: DecimalNode, picture: TextNode, options: TextNode?): TextNode {
        TODO("Not yet implemented")
    }

    override fun fromMillis(number: DecimalNode, picture: TextNode?, timezone: TextNode?): TextNode {
        val dtf = (picture
            ?.let { DateTimeFormatter.ofPattern(picture.asText()) }
            ?: DateTimeFormatter.ISO_INSTANT)
            .withZone(timezone
                ?.let { ZoneId.of(it.asText()) }
                ?: ZoneId.systemDefault()
            )
        return TextNode(dtf.format(Instant.ofEpochMilli(number.longValue())))
    }


    override fun join(array: ArrayNode, separator: TextNode): TextNode {
        return TextNode(array.joinToString(separator.textValue()))
    }

    override fun keys(arr: ArrayNode): ArrayNode {
        val keys = mutableSetOf<String>()
        arr.forEach { node ->
            when (node) {
                is ArrayNode -> keys.addAll(keys(node).map { it.textValue() })
                is ObjectNode -> keys.addAll(keys(node).map { it.textValue() })
            }
        }
        return processor.nf.arrayNode().addAll(keys.map { TextNode(it) })
    }

    override fun keys(obj: ObjectNode): ArrayNode {
        val keys = mutableSetOf<String>()
        obj.fieldNames().forEach { fieldName ->
            keys.add(fieldName)
        }
        return processor.nf.arrayNode().addAll(keys.map { TextNode(it) })
    }

    override fun length(str: TextNode): DecimalNode {
        return DecimalNode(string(str).textValue().length.toBigDecimal())
    }

    override fun lowercase(str: TextNode): TextNode {
        return TextNode(str.textValue().lowercase())
    }

    override fun lookup(array: ArrayNode, key: TextNode): JsonNode? {
        val res = processor.nf.arrayNode()
        array.forEach { node ->
            if (node is ObjectNode) {
                lookup(node, key)?.let { res.add(it) }
            }
        }
        return if (res.isEmpty) null else res
    }

    override fun lookup(obj: ObjectNode, key: TextNode): JsonNode? {
        return when (obj.has(key.textValue())) {
            true -> obj[key.textValue()]
            else -> null
        }
    }

    override fun map(array: ArrayNode, function: FunNode): ArrayNode {
        TODO("Not yet implemented")
    }

    override fun match(str: JsonNode, pattern: RegexNode, limit: DecimalNode?) {
        pattern.regex.findAll(str.textValue()).forEachIndexed { index, matchResult ->
            if (index < (limit?.asInt() ?: Int.MAX_VALUE)) {
                processor.nf.objectNode()
                    .put(TAG_MATCH, matchResult.value)
                    .put(TAG_INDEX, matchResult.range.first)
                    .set<ObjectNode>(
                        TAG_GROUPS,
                        processor.nf.arrayNode().add(matchResult.groupValues.map { TextNode(it) }.last())
                    )
            }
        }
    }

    override fun max(array: ArrayNode): DecimalNode {
        return DecimalNode(array.filterIsInstance<NumericNode>().maxOf { it.asDouble() }.toBigDecimal())
    }

    override fun merge(array: ArrayNode): ObjectNode {
        val res = processor.nf.objectNode()
        array.filterIsInstance<ObjectNode>().forEach { obj ->
            obj.fieldNames().forEach { fieldName ->
                res.set<JsonNode>(fieldName, obj[fieldName])
            }
        }
        return res
    }

    override fun millis(): DecimalNode {
        return DecimalNode(processor.time.toEpochMilli().toBigDecimal())
    }

    override fun min(array: ArrayNode): DecimalNode {
        return DecimalNode(array.filterIsInstance<NumericNode>().minOf { it.asDouble() }.toBigDecimal())
    }

    override fun not(arg: BooleanNode): BooleanNode {
        return BooleanNode.valueOf(!arg.booleanValue())
    }

    override fun now(picture: TextNode?, timezone: TextNode?): TextNode {
        val dtf = (
                picture
                    ?.let { DateTimeFormatter.ofPattern(picture.asText()) }
                    ?: DateTimeFormatter.ISO_INSTANT
                )
            .withZone(
                timezone
                    ?.let { ZoneId.of(it.asText()) }
                    ?: ZoneId.systemDefault()
            )
        return TextNode(dtf.format(processor.time))
    }

    override fun number(arg: JsonNode?): DecimalNode {
        return DecimalNode(
            when (arg) {
                is BooleanNode -> when (arg.booleanValue()) {
                    true -> BigDecimal.ONE
                    else -> BigDecimal.ZERO
                }

                is NumericNode -> arg.decimalValue()
                is TextNode -> BigDecimal(arg.textValue())
                else -> throw IllegalArgumentException("$arg can't cast to number")
            }
        )
    }

    override fun pad(str: TextNode, width: DecimalNode, char: TextNode): TextNode {
        val w = width.asInt()
        return TextNode(
            when {
                w < 0 -> str.textValue().padStart(-w, char.textValue()[0])
                else -> str.textValue().padEnd(w, char.textValue()[0])
            }
        )
    }

    override fun parseInteger(string: TextNode, picture: TextNode): DecimalNode {
        TODO("Not yet implemented")
    }

    override fun power(base: DecimalNode, exponent: DecimalNode): DecimalNode {
        return DecimalNode(base.decimalValue().pow(exponent.asInt()))
    }

    override fun random(): DecimalNode {
        return DecimalNode(processor.random.nextDouble().toBigDecimal())
    }

    override fun reduce(array: ArrayNode, function: FunNode, init: FunNode): JsonNode {
        TODO("Not yet implemented")
    }

    // todo: implement limit
    override fun replace(str: TextNode, pattern: RegexNode, replacement: TextNode, limit: DecimalNode?): TextNode {
        return TextNode(str.textValue().replace(pattern.regex, replacement.textValue()))
    }

    // todo: implement limit
    override fun replace(str: TextNode, pattern: TextNode, replacement: TextNode, limit: DecimalNode?): TextNode {
        return TextNode(str.textValue().replace(pattern.textValue(), replacement.textValue()))
    }

    override fun reverse(array: ArrayNode): ArrayNode {
        return processor.nf.arrayNode().addAll(array.reversed())
    }

    override fun round(number: DecimalNode, precision: DecimalNode?): DecimalNode {
        val scale = precision?.asInt() ?: 0
        return DecimalNode(number.decimalValue().setScale(scale, RoundingMode.HALF_EVEN))
    }

    override fun shuffle(array: ArrayNode): ArrayNode {
        return processor.nf.arrayNode().addAll(array.shuffled(processor.random))
    }

    override fun sift(obj: ObjectNode, function: FunNode): JsonNode {
        TODO("Not yet implemented")
    }

    override fun single(array: ArrayNode, function: FunNode): JsonNode {
        TODO("Not yet implemented")
    }

    override fun sort(array: ArrayNode, function: FunNode?): ArrayNode {
        TODO("Not yet implemented")
    }

    override fun split(str: TextNode, separator: RegexNode, limit: DecimalNode?): ArrayNode {
        return processor.nf.arrayNode().addAll(
            str.textValue()
                .split(separator.regex, limit?.asInt() ?: 0)
                .map { TextNode(it) }
        )
    }

    override fun split(str: TextNode, separator: TextNode, limit: DecimalNode?): ArrayNode {
        return processor.nf.arrayNode().addAll(
            str.textValue()
                .split(separator.asText(), ignoreCase = false, limit = limit?.asInt() ?: 0)
                .map { TextNode(it) }
        )
    }

    override fun spread(array: ArrayNode): ArrayNode {
        val res = processor.nf.arrayNode()
        array.filterIsInstance<ObjectNode>().forEach { obj ->
            res.addAll(spread(obj))
        }
        return res
    }

    override fun spread(obj: ObjectNode): ArrayNode {
        val res = processor.nf.arrayNode()
        obj.fieldNames().forEach { fieldName ->
            res.add(processor.nf.objectNode().set<JsonNode>(fieldName, obj[fieldName]))
        }
        return res
    }

    override fun sqrt(number: DecimalNode): DecimalNode {
        return DecimalNode(kotlin.math.sqrt(number.asDouble()).toBigDecimal())
    }

    override fun string(arg: JsonNode?, prettify: BooleanNode?): TextNode {
        return when (arg) {
            is TextNode -> arg
            else -> TextNode(
                when (prettify?.asBoolean() ?: false) {
                    true -> processor.om.writerWithDefaultPrettyPrinter().writeValueAsString(arg)
                    else -> processor.om.writeValueAsString(arg)
                }
            )
        }
    }

    override fun substring(str: TextNode, start: DecimalNode, length: DecimalNode?): TextNode {
        val txt = string(str).textValue()
        val first = 0
            .coerceAtLeast(if (start.intValue() < 0) txt.length + start.intValue() else start.intValue())
            .coerceAtMost(txt.length)
        val last = 0
            .coerceAtLeast(length?.let { first + length.asInt() } ?: txt.length)
            .coerceAtMost(txt.length)
        return TextNode(txt.substring(first, last))
    }

    override fun substringAfter(str: TextNode, chars: TextNode): TextNode {
        return TextNode(str.textValue().substringAfter(chars.textValue()))
    }

    override fun substringBefore(str: TextNode, chars: TextNode): TextNode {
        return TextNode(str.textValue().substringBefore(chars.textValue()))
    }

    override fun sum(array: ArrayNode): DecimalNode {
        return DecimalNode(array.filterIsInstance<NumericNode>().sumOf { it.decimalValue() })
    }

    override fun toMillis(timestamp: TextNode, picture: TextNode?): DecimalNode {
        val dtf = (picture
            ?.let { DateTimeFormatter.ofPattern(picture.asText()) }
            ?: DateTimeFormatter.ISO_INSTANT)
            .withZone(ZoneId.systemDefault())
        return DecimalNode(Instant.from(dtf.parse(timestamp.asText())).toEpochMilli().toBigDecimal())
    }

    override fun trim(str: TextNode): TextNode {
        return TextNode(str.textValue().replace(whitespaceRegex, " ").trim())
    }

    override fun type(value: JsonNode?): TextNode {
        return TextNode(
            when (value) {
                null -> IS_NULL
                is ArrayNode -> IS_ARRAY
                is BooleanNode -> IS_BOOLEAN
                is FunNode -> IS_FUNCTION
                is NumericNode -> IS_NUMBER
                is ObjectNode -> IS_OBJECT
                is TextNode -> IS_STRING
                else -> IS_UNDEFINED
            }
        )
    }

    override fun uppercase(str: TextNode): TextNode {
        return TextNode(str.textValue().uppercase())
    }

    override fun zip(vararg arrays: ArrayNode): ArrayNode {
        var len = Int.MAX_VALUE
        arrays.forEach { array ->
            len = min(len, array.size())
        }
        val res = processor.nf.arrayNode()
        for (i in 0 until len) {
            res.add(processor.nf.arrayNode())
            for (j in arrays.indices) {
                if (i < arrays[j].size()) {
                    (res[i] as ArrayNode).add(arrays[j][i])
                }
            }
        }
        return res
    }

}

