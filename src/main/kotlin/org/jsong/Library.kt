/**
 * MIT License
 *
 * Copyright (c) [2023] [Luca Nicola Debiasi]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
import kotlin.random.Random


class Library(
    val objectMapper: ObjectMapper,
    val random: Random,
    val time: Instant
) : JSONataFunctionLibrary {

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
         * Used in [match] method.
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

    enum class Type(val descriptor: TextNode) {
        ARRAY(TextNode("array")),
        BOOLEAN(TextNode("boolean")),
        NULL(TextNode("null")),
        NUMBER(TextNode("number")),
        OBJECT(TextNode("object")),
        STRING(TextNode("string")),
        @Suppress("unused")
        UNDEFINED(TextNode("undefined"))
    }

    override fun abs(number: DecimalNode): DecimalNode {
        return DecimalNode(number.decimalValue().abs())
    }

    /**
     * See [JSONataFunctionLibrary.append].
     */
    override fun append(array1: JsonNode, array2: JsonNode): ArrayNode {
        val arr1 = expand(array1)
        val arr2 = expand(array2)
        return when {
            arr1 is RangesNode && arr2 is RangesNode -> {
                RangesNode(objectMapper.nodeFactory).addAll(arr1).addAll(arr2)
            }

            else -> {
                objectMapper.nodeFactory.arrayNode().addAll(arr1).addAll(arr2)
            }
        }
    }

    override fun assert(condition: JsonNode, message: JsonNode): BooleanNode {
        if (!boolean(condition).booleanValue()) {
            throw AssertionError(string(message).textValue())
        }
        return BooleanNode.TRUE
    }

    override fun average(array: JsonNode): DecimalNode {
        val arr = expand(array)
        return DecimalNode(sum(arr).decimalValue().divide(arr.size().toBigDecimal()))
    }

    override fun base64decode(str: JsonNode): TextNode {
        return TextNode(Base64.getDecoder().decode(string(str).textValue()).toString(Charsets.UTF_8))
    }

    override fun base64encode(str: JsonNode): TextNode {
        return TextNode(Base64.getEncoder().encodeToString(string(str).textValue().toByteArray()))
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
            is FunctionNode -> false
            is NumericNode -> arg.decimalValue() != BigDecimal.ZERO
            is ObjectNode -> !arg.isEmpty
            is TextNode -> arg.textValue().isNotBlank()
            else -> false
        })
    }

    override fun ceil(number: DecimalNode): DecimalNode {
        return DecimalNode(kotlin.math.ceil(number.asDouble()).toBigDecimal())
    }

    override fun contains(str: JsonNode, pattern: JsonNode): BooleanNode {
        val txt = string(str).textValue()
        return BooleanNode.valueOf(
            when (pattern) {
                is RegexNode -> txt.contains(pattern.regex)
                else -> txt.contains(string(pattern).textValue())
            }
        )
    }

    /**
     * See [JSONataFunctionLibrary.count].
     */
    override fun count(array: JsonNode): DecimalNode {
        val arr = expand(array)
        return DecimalNode(
            when (arr) {
                is RangesNode -> arr.indexes.size().toBigDecimal()
                else -> arr.size().toBigDecimal()
            }
        )
    }

    override fun decodeUrl(str: JsonNode): TextNode {
        return TextNode(URLDecoder.decode(string(str).textValue(), Charsets.UTF_8.toString()))
    }

    override fun decodeUrlComponent(str: JsonNode): TextNode {
        return TextNode(URLDecoder.decode(string(str).textValue(), Charsets.UTF_8.toString()))
    }

    /**
     * See [JSONataFunctionLibrary.distinct].
     */
    override fun distinct(array: JsonNode): ArrayNode {
        return objectMapper.nodeFactory.arrayNode().addAll(when(array) {
            is RangesNode -> array.indexes.toSet()
            else -> expand(array).toSet()
        })
    }

    override fun each(obj: ObjectNode, function: FunctionNode): ArrayNode {
        TODO("Not yet implemented")
    }

    override fun error(message: JsonNode?) {
        throw message?.let { Error(it.textValue()) } ?: Error()
    }
    
    override fun encodeUrl(str: JsonNode): TextNode {
        return TextNode(URLEncoder.encode(str.textValue(), Charsets.UTF_8.toString()))
    }

    override fun encodeUrlComponent(str: JsonNode): TextNode {
        return TextNode(URLEncoder.encode(str.textValue(), Charsets.UTF_8.toString()))
    }

    override fun eval(expr: JsonNode, context: JsonNode?): JsonNode? {
        TODO("return Processor(context).evaluate(string(expr).textValue())")
    }

    override fun exists(arg: JsonNode?): BooleanNode {
        return BooleanNode.valueOf(arg != null)
    }

    /**
     * Return an [ArrayNode]
     * * empty if [node] is null,
     * * being a [RangesNode] instance if [node] is a [RangesNode] instance,
     * * having the [node] element if [node] isn't an array,
     * * the [node] itself if it is a not empty array.
     */
    private fun expand(
        node: JsonNode?
    ): ArrayNode {
        return when (node) {
            null -> objectMapper.nodeFactory.arrayNode()
            is RangeNode -> RangesNode(objectMapper.nodeFactory).add(node)
            is ArrayNode -> node
            else -> objectMapper.nodeFactory.arrayNode().add(node)
        }
    }

    override fun filter(array: ArrayNode, function: FunctionNode): ArrayNode {
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


    override fun join(array: JsonNode, separator: JsonNode?): TextNode {
        val arr = when(array) {
            is RangesNode -> array.indexes
            else -> expand(array)
        }.map { string(it).textValue() }.toTypedArray()
        val sep = separator?.let { string(it).textValue() } ?: ""
        return TextNode(arr.joinToString(sep))
    }

    override fun keys(array: ArrayNode): ArrayNode {
        val keys = mutableSetOf<String>()
        when(array) {
            is RangesNode -> keys.addAll(keys(array.indexes).map { it.textValue() })
            else -> array.forEach { node ->
                when (node) {
                    is ArrayNode -> keys.addAll(keys(node).map { it.textValue() })
                    is ObjectNode -> keys.addAll(keys(node).map { it.textValue() })
                }
            }
        }
        return objectMapper.nodeFactory.arrayNode().addAll(keys.map { TextNode(it) })
    }

    override fun keys(obj: ObjectNode): ArrayNode {
        val keys = mutableSetOf<String>()
        obj.fieldNames().forEach { fieldName ->
            keys.add(fieldName)
        }
        return objectMapper.nodeFactory.arrayNode().addAll(keys.map { TextNode(it) })
    }

    override fun length(str: JsonNode): DecimalNode {
        return DecimalNode(string(str).textValue().length.toBigDecimal())
    }

    override fun lowercase(str: JsonNode): TextNode {
        return TextNode(string(str).textValue().lowercase())
    }

    override fun lookup(array: ArrayNode, key: TextNode): JsonNode? {
        val result = objectMapper.nodeFactory.arrayNode()
        array.forEach { node ->
            if (node is ObjectNode) {
                lookup(node, key)?.let { result.add(it) }
            }
        }
        return if (result.isEmpty) null else result
    }

    override fun lookup(obj: ObjectNode, key: TextNode): JsonNode? {
        return when (obj.has(key.textValue())) {
            true -> obj[key.textValue()]
            else -> null
        }
    }

    override fun map(array: ArrayNode, function: FunctionNode): ArrayNode {
        TODO("Not yet implemented")
    }

    override fun match(str: JsonNode, pattern: JsonNode, limit: JsonNode?): ArrayNode {
        val result = objectMapper.nodeFactory.arrayNode()
        val txt = string(str).textValue()
        val ptt = when (pattern) {
            is RegexNode -> pattern
            else -> RegexNode(string(pattern).textValue())
        }
        val lim = limit?.let { number(it).asInt() } ?: Int.MAX_VALUE
        ptt.regex.findAll(txt).forEachIndexed { i, matchResult ->
            if (i < lim) {
                result.add(
                    objectMapper.nodeFactory.objectNode()
                        .put(TAG_MATCH, matchResult.value)
                        .put(TAG_INDEX, matchResult.range.first)
                        .set<ObjectNode>(
                            TAG_GROUPS,
                            objectMapper.nodeFactory.arrayNode().add(
                                matchResult.groupValues.map { TextNode(it) }.last()
                            )
                        )
                )
            }
        }
        return result
    }

    override fun max(array: JsonNode): DecimalNode {
        return DecimalNode(when(array) {
            is RangesNode -> array.indexes.maxOf{ it.asInt()}.toBigDecimal()
            else -> expand(array)
                .filterIsInstance<NumericNode>()
                .maxOf { it.asDouble() }
                .toBigDecimal()
        })
    }

    override fun merge(array: ArrayNode): ObjectNode {
        val result = objectMapper.nodeFactory.objectNode()
        array.filterIsInstance<ObjectNode>().forEach { obj ->
            obj.fieldNames().forEach { fieldName ->
                result.set<JsonNode>(fieldName, obj[fieldName])
            }
        }
        return result
    }

    override fun millis(): DecimalNode {
        return DecimalNode(time.toEpochMilli().toBigDecimal())
    }

    override fun min(array: JsonNode): DecimalNode {
        return DecimalNode(when(array) {
            is RangesNode -> array.indexes.minOf{ it.asInt()}.toBigDecimal()
            else -> expand(array)
                .filterIsInstance<NumericNode>()
                .minOf { it.asDouble() }
                .toBigDecimal()
        })
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
        return TextNode(dtf.format(time))
    }

    override fun number(arg: JsonNode?): DecimalNode {
        return DecimalNode(
            when (arg) {
                null -> BigDecimal.ZERO // todo: to remove in future version because not documented cast
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

    override fun pad(str: JsonNode, width: JsonNode, char: JsonNode?): TextNode {
        val w = number(width).asInt()
        val filler = char?.let { string(char).textValue()[0] } ?: ' '
        return TextNode(
            when {
                w < 0 -> string(str).textValue().padStart(-w, filler)
                else -> string(str).textValue().padEnd(w, filler)
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
        return DecimalNode(random.nextDouble().toBigDecimal())
    }

    override fun reduce(array: ArrayNode, function: FunctionNode, init: FunctionNode): JsonNode {
        TODO("Not yet implemented")
    }

    // todo: implement limit
    override fun replace(
        str: JsonNode,
        pattern: JsonNode,
        replacement: JsonNode,
        limit: JsonNode?): TextNode {
        val txt = string(str).textValue()
        val new = string(replacement).textValue()
        return TextNode(
            when (pattern) {
                is RegexNode -> txt.replace(pattern.regex, new)
                else -> txt.replace(string(pattern).textValue(), new)
            }
        )
    }

    /**
     * See [JSONataFunctionLibrary.reverse].
     */
    override fun reverse(array: JsonNode): ArrayNode {
        return objectMapper.nodeFactory.arrayNode().addAll(when(array) {
            is RangesNode -> array.indexes.reversed()
            else -> expand(array).reversed()
        })
    }

    override fun round(number: DecimalNode, precision: DecimalNode?): DecimalNode {
        val scale = precision?.asInt() ?: 0
        return DecimalNode(number.decimalValue().setScale(scale, RoundingMode.HALF_EVEN))
    }

    /**
     * See [JSONataFunctionLibrary.shuffle].
     */
    override fun shuffle(array: JsonNode): ArrayNode {
        return objectMapper.nodeFactory.arrayNode().addAll(when (array) {
            is RangesNode -> array.indexes.shuffled(random)
            else -> expand(array).shuffled(random)
        })
    }

    override fun sift(obj: ObjectNode, function: FunctionNode): JsonNode {
        TODO("Not yet implemented")
    }

    override fun single(array: ArrayNode, function: FunctionNode): JsonNode {
        TODO("Not yet implemented")
    }

    override fun sort(array: JsonNode, function: FunctionNode?): ArrayNode {
        // val array = expand(node)
        TODO("Not yet implemented")
    }

    override fun split(str: JsonNode, separator: JsonNode, limit: JsonNode?): ArrayNode {
        val _str = string(str).textValue()
        val _limit = limit?.let { number(it).asInt() } ?: 0
        val _split = when (separator) {
            is RegexNode -> _str.split(separator.regex, _limit)
            else -> _str.split((string(separator).textValue()), ignoreCase = false, limit = _limit)
        }.map { TextNode(it) }
        return objectMapper.nodeFactory.arrayNode().addAll(_split)
    }

    override fun spread(array: ArrayNode): ArrayNode {
        val result = objectMapper.nodeFactory.arrayNode()
        expand(array)
            .filterIsInstance<ObjectNode>()
            .forEach { obj ->
                result.addAll(spread(obj))
            }
        return result
    }

    override fun spread(obj: ObjectNode): ArrayNode {
        val result = objectMapper.nodeFactory.arrayNode()
        obj.fieldNames().forEach { fieldName ->
            result.add(objectMapper.nodeFactory.objectNode().set<JsonNode>(fieldName, obj[fieldName]))
        }
        return result
    }

    override fun sqrt(number: DecimalNode): DecimalNode {
        return DecimalNode(kotlin.math.sqrt(number.asDouble()).toBigDecimal())
    }

    override fun string(arg: JsonNode?, prettify: BooleanNode?): TextNode {
        return when (arg) {
            is TextNode -> arg
            else -> TextNode(
                when (prettify?.asBoolean() ?: false) {
                    true -> objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arg)
                    else -> objectMapper.writeValueAsString(arg)
                }
            )
        }
    }

    override fun substring(str: JsonNode, start: DecimalNode, length: DecimalNode?): TextNode {
        val txt = string(str).textValue()
        val first = 0
            .coerceAtLeast(if (start.intValue() < 0) txt.length + start.intValue() else start.intValue())
            .coerceAtMost(txt.length)
        val last = 0
            .coerceAtLeast(length?.let { first + length.asInt() } ?: txt.length)
            .coerceAtMost(txt.length)
        return TextNode(txt.substring(first, last))
    }

    override fun substringAfter(str: JsonNode, chars: TextNode): TextNode {
        return TextNode(string(str).textValue().substringAfter(chars.textValue()))
    }

    override fun substringBefore(str: JsonNode, chars: TextNode): TextNode {
        return TextNode(string(str).textValue().substringBefore(chars.textValue()))
    }

    override fun sum(array: JsonNode): DecimalNode {
        return DecimalNode(
            expand(array)
                .filterIsInstance<NumericNode>()
                .sumOf { it.decimalValue() }
        )
    }

    override fun toMillis(timestamp: TextNode, picture: TextNode?): DecimalNode {
        val dtf = (picture
            ?.let { DateTimeFormatter.ofPattern(picture.asText()) }
            ?: DateTimeFormatter.ISO_INSTANT)
            .withZone(ZoneId.systemDefault())
        return DecimalNode(Instant.from(dtf.parse(timestamp.asText())).toEpochMilli().toBigDecimal())
    }

    override fun trim(str: JsonNode): TextNode {
        return TextNode(string(str).textValue().replace(whitespaceRegex, " ").trim())
    }

    override fun type(value: JsonNode?): TextNode {
        return TextNode(
            when (value) {
                null -> IS_NULL
                is NullNode -> IS_NULL
                is ArrayNode -> IS_ARRAY
                is BooleanNode -> IS_BOOLEAN
                is FunctionNode -> IS_FUNCTION
                is NumericNode -> IS_NUMBER
                is ObjectNode -> IS_OBJECT
                is TextNode -> IS_STRING
                else -> IS_UNDEFINED
            }
        )
    }

    override fun uppercase(str: JsonNode): TextNode {
        return TextNode(string(str).textValue().uppercase())
    }

    override fun zip(vararg arrays: JsonNode): ArrayNode {
        val matrix = arrays.map { expand(it) }
        var len = Int.MAX_VALUE
        matrix.forEach { array ->
            len = min(len, array.size())
        }
        val result = objectMapper.nodeFactory.arrayNode()
        for (i in 0 until len) {
            result.add(objectMapper.nodeFactory.arrayNode())
            for (j in matrix.indices) {
                if (i < matrix[j].size()) {
                    (result[i] as ArrayNode).add(matrix[j][i])
                }
            }
        }
        return result
    }

}

