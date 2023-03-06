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
import java.math.MathContext
import java.math.RoundingMode
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

/**
 * @property objectMapper is used to create [JsonNode] instances.
 * @property random is used in [random] and [shuffle] methods.
 * @property time sets the instant the [Processor] evaluates code: used in [millis] and [now] methods.
 */
class Library(
    val mathContext: MathContext,
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

    /**
     * See [JSONataFunctionLibrary.abs].
     *
     * @param number cast calling [JSONataFunctionLibrary.number].
     */
    override fun abs(
        number: DecimalNode
    ): DecimalNode {
        return DecimalNode(number(number).decimalValue().abs())
    }

    /**
     * See [JSONataFunctionLibrary.append].
     *
     * @param array1 is cast to [ArrayNode] calling [expand].
     * @param array2 is cast to [ArrayNode] calling [expand].
     */
    override fun append(
        array1: JsonNode,
        array2: JsonNode
    ): ArrayNode {
        val expanded1 = expand(array1)
        val expanded2 = expand(array2)
        return when {
            expanded1 is RangesNode && expanded2 is RangesNode -> {
                RangesNode(objectMapper.nodeFactory).addAll(expanded1).addAll(expanded2)
            }

            else -> {
                objectMapper.nodeFactory.arrayNode().addAll(expanded1).addAll(expanded2)
            }
        }
    }

    /**
     * See [JSONataFunctionLibrary.assert].
     *
     * @param condition is cast calling [JSONataFunctionLibrary.boolean].
     * @param message is cast calling [JsonNode.textValue].
     *
     * @throws AssertionError if [condition] result to be `true`.
     */
    @Throws (
        AssertionError::class
    )
    override fun assert(
        condition: JsonNode,
        message: JsonNode
    ): BooleanNode {
        if (!boolean(condition).booleanValue()) {
            throw AssertionError(string(message).textValue())
        }
        return BooleanNode.TRUE
    }

    /**
     * See [JSONataFunctionLibrary.average].
     *
     * @see sum
     */
    override fun average(
        array: JsonNode
    ): DecimalNode {
        val arr = expand(array)
        return DecimalNode(sum(arr).decimalValue().divide(arr.size().toBigDecimal()))
    }

    /**
     * See [JSONataFunctionLibrary.base64decode].
     *
     * @param str cast to string calling [JSONataFunctionLibrary.string].
     */
    override fun base64decode(
        str: JsonNode
    ): TextNode {
        return TextNode(Base64.getDecoder().decode(string(str).textValue()).toString(Charsets.UTF_8))
    }

    /**
     * See [JSONataFunctionLibrary.base64encode].
     *
     * @param str cast to string calling [JSONataFunctionLibrary.string].
     */
    override fun base64encode(
        str: JsonNode): TextNode {
        return TextNode(Base64.getEncoder().encodeToString(string(str).textValue().toByteArray()))
    }

    /**
     * See [JSONataFunctionLibrary.boolean].
     */
    override fun boolean(
        arg: JsonNode?
    ): BooleanNode {
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
     *
     * @param array is cast to [ArrayNode] calling [expand].
     */
    override fun count(
        array: JsonNode
    ): DecimalNode {
        val expanded = expand(array)
        return DecimalNode(
            when (expanded) {
                is RangesNode -> expanded.indexes.size().toBigDecimal()
                else -> expanded.size().toBigDecimal()
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
     *
     * @param array is cast to [ArrayNode] calling [expand].
     */
    override fun distinct(
        array: JsonNode
    ): ArrayNode {
        return objectMapper.nodeFactory.arrayNode().addAll(
            when (array) {
                is RangesNode -> array.indexes.toSet()
                else -> expand(array).toSet()
            }
        )
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

    /**
     * See [JSONataFunctionLibrary.eval].
     *
     * This function creates a new processor to evaluate [expr],
     * inheriting [mathContext], [objectMapper], [random], [time] and this [JSONataFunctionLibrary].
     * The processor evaluating [expr] has its own variable registry.
     *
     * @param expr  to evaluate as a JSONata/JSong expression, cast to string calling [JSONataFunctionLibrary.string].
     *
     * @param context of the [expr] evaluation, it can be `null`.
     */
    override fun eval(
        expr: JsonNode,
        context: JsonNode?
    ): JsonNode? {
        return Processor(context, mutableMapOf(), mathContext, objectMapper, random, time, this)
            .evaluate(string(expr).asText())
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
        val arr = when (array) {
            is RangesNode -> array.indexes
            else -> expand(array)
        }.map { string(it).textValue() }.toTypedArray()
        val sep = separator?.let { string(it).textValue() } ?: ""
        return TextNode(arr.joinToString(sep))
    }

    override fun keys(array: ArrayNode): ArrayNode {
        val keys = mutableSetOf<String>()
        when (array) {
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
        return DecimalNode(when (array) {
            is RangesNode -> array.indexes.maxOf { it.asInt() }.toBigDecimal()
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
        return DecimalNode(when (array) {
            is RangesNode -> array.indexes.minOf { it.asInt() }.toBigDecimal()
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
        limit: JsonNode?
    ): TextNode {
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
     *
     * @param array is cast to [ArrayNode] calling [expand].
     */
    override fun reverse(
        array: JsonNode
    ): ArrayNode {
        return objectMapper.nodeFactory.arrayNode().addAll(
            when (array) {
                is RangesNode -> array.indexes.reversed()
                else -> expand(array).reversed()
            }
        )
    }

    override fun round(number: DecimalNode, precision: DecimalNode?): DecimalNode {
        val scale = precision?.asInt() ?: 0
        return DecimalNode(number.decimalValue().setScale(scale, RoundingMode.HALF_EVEN))
    }


    override fun sift(obj: ObjectNode, function: FunctionNode): JsonNode {
        TODO("Not yet implemented")
    }

    override fun single(array: ArrayNode, function: FunctionNode): JsonNode {
        TODO("Not yet implemented")
    }

    /**
     * See [JSONataFunctionLibrary.shuffle].
     *
     * The [random] property is used to shuffle.
     *
     * @param array is cast to [ArrayNode] calling [expand].
     */
    override fun shuffle(
        array: JsonNode
    ): ArrayNode {
        return objectMapper.nodeFactory.arrayNode().addAll(
            when (array) {
                is RangesNode -> array.indexes.shuffled(random)
                else -> expand(array).shuffled(random)
            }
        )
    }

    /**
     * See [JSONataFunctionLibrary.sort].
     *
     * @param array is cast to [ArrayNode] calling [expand]:
     * not numeric elements are converted in strings calling the [string] method.
     *
     * @param function can be `null`, in this case the array is sorted in ascending order,
     * else [function] must accept two arguments:
     * the values of the two arguments are a couple of elements of the [array] parameter.
     * The result of [function] is cast with [boolean], if `true` the values of the
     * `two` arguments are swapped in the returned array.
     *
     * @return the [array] sorted.
     *
     * @throws FunctionTypeException if [function] is not a [FunctionNode]
     * or doesn't accept two arguments in its signature.
     */
    @Throws(
        FunctionTypeException::class
    )
    override fun sort(
        array: JsonNode,
        function: JsonNode?
    ): ArrayNode {
        val expanded = expand(array)
        return when (function) {
            null -> objectMapper.nodeFactory.arrayNode().addAll(expanded.sortedWith { o1, o2 ->
                when {
                    (o1?.isNumber ?: false) && (o2?.isNumber ?: false) -> {
                        number(o1).decimalValue().compareTo(number(o2).decimalValue())
                    }

                    else -> string(o1).asText().compareTo(string(o2).textValue())
                }
            })

            is FunctionNode -> when (function.args.size == 2) {
                true -> when (expanded.size()) {
                    0 -> expanded
                    1 -> expanded
                    else -> {
                        for (i in 1 until expanded.size()) {
                            val varMap = mutableMapOf<String, JsonNode?>()
                            varMap[function.args[0]] = expanded[i - 1]
                            varMap[function.args[1]] = expanded[i]
                            val toSwap = boolean(
                                Processor(
                                    null,
                                    varMap,
                                    mathContext,
                                    objectMapper,
                                    random,
                                    time,
                                    this
                                ).evaluate(function.body)
                            )
                            if (toSwap.booleanValue()) {
                                expanded[i - 1] = varMap[function.args[1]]
                                expanded[i] = varMap[function.args[0]]
                            }
                        }
                        expanded
                    }
                }

                else -> throw FunctionTypeException("$function must have two arguments.")
            }

            else -> throw FunctionTypeException.forNode(function)
        }
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

    /**
     * `null` values are represented
     */
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

    /**
     * See [JSONataFunctionLibrary.zip].
     *
     * @param arrays elements are cast with [expand].
     */
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

} //~ Library

