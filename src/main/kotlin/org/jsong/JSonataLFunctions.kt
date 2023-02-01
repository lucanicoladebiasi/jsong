package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*

interface JSonataLFunctions {

    // https://docs.jsonata.org/array-functions

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    fun append(node1: JsonNode, node2: JsonNode): ArrayNode

    /**
     * https://docs.jsonata.org/array-functions#count
     */
    fun count(node: JsonNode): DecimalNode

    /**
     * https://docs.jsonata.org/array-functions#distinct
     */
    fun distinct(node: JsonNode): ArrayNode

    /**
     * https://docs.jsonata.org/array-functions#reverse
     */
    fun reverse(node: JsonNode): ArrayNode

    /**
     * https://docs.jsonata.org/array-functions#shuffle
     */
    fun shuffle(node: JsonNode): ArrayNode

    /**
     * https://docs.jsonata.org/array-functions#sort
     */
    fun sort(node: JsonNode, function: FunNode?): ArrayNode

    /**
     * https://docs.jsonata.org/array-functions#zip
     */
    fun zip(vararg nodes: JsonNode): ArrayNode

    // https://docs.jsonata.org/boolean-functions

    /**
    https://docs.jsonata.org/boolean-functions#boolean
     */
    fun boolean(arg: JsonNode?): BooleanNode

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    fun not(arg: BooleanNode): BooleanNode

    /**
     *
     */
    fun exists(arg: JsonNode?): BooleanNode

    // https://docs.jsonata.org/date-time-functions

    /**
     * https://docs.jsonata.org/date-time-functions#frommillis
     */
    fun fromMillis(number: DecimalNode, picture: TextNode? = null, timezone: TextNode? = null): TextNode

    /**
     * https://docs.jsonata.org/date-time-functions#millis
     */
    fun millis(): DecimalNode

    /**
     * https://docs.jsonata.org/date-time-functions#now
     */
    fun now(picture: TextNode? = null, timezone: TextNode? = null): TextNode

    /**
     * https://docs.jsonata.org/date-time-functions#tomillis
     */
    fun toMillis(timestamp: TextNode, picture: TextNode? = null): DecimalNode


    // https://docs.jsonata.org/higher-order-functions

    /**
     * https://docs.jsonata.org/higher-order-functions#filter
     */
    fun filter(array: ArrayNode, function: FunNode): ArrayNode

    /**
     * https://docs.jsonata.org/higher-order-functions#map
     */
    fun map(array: ArrayNode, function: FunNode): ArrayNode

    /**
     * https://docs.jsonata.org/higher-order-functions#reduce
     */
    fun reduce(array: ArrayNode, function: FunNode, init: FunNode): JsonNode

    /**
     * https://docs.jsonata.org/higher-order-functions#reduce
     */
    fun sift(obj: ObjectNode, function: FunNode): JsonNode

    /**
     * https://docs.jsonata.org/higher-order-functions#single
     */
    fun single(array: ArrayNode, function: FunNode): JsonNode


    // https://docs.jsonata.org/aggregation-functions

    /**
     * https://docs.jsonata.org/aggregation-functions#average
     */
    fun average(node: JsonNode): DecimalNode

    /**
     * https://docs.jsonata.org/aggregation-functions#max
     */
    fun max(node: JsonNode): DecimalNode

    /**
     * https://docs.jsonata.org/aggregation-functions#min
     */
    fun min(node: JsonNode): DecimalNode

    /**
     * https://docs.jsonata.org/aggregation-functions#sum
     */
    fun sum(node: JsonNode): DecimalNode


    // https://docs.jsonata.org/numeric-functions

    /**
     * https://docs.jsonata.org/numeric-functions#abs
     */
    fun abs(number: DecimalNode): DecimalNode

    /**
     * https://docs.jsonata.org/numeric-functions#ceil
     */
    fun ceil(number: DecimalNode): DecimalNode

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    fun floor(number: DecimalNode): DecimalNode

    /**
     * https://docs.jsonata.org/numeric-functions#formatbase
     */
    fun formatBase(number: DecimalNode, radix: DecimalNode? = null): TextNode

    /**
     * https://docs.jsonata.org/numeric-functions#formatinteger
     */
    fun formatInteger(number: DecimalNode, picture: TextNode): TextNode

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    fun formatNumber(number: DecimalNode, picture: TextNode, options: TextNode?): TextNode

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    fun number(arg: JsonNode?): DecimalNode

    /**
     * https://docs.jsonata.org/numeric-functions#random
     */
    fun parseInteger(string: TextNode, picture: TextNode): DecimalNode

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    fun power(base: DecimalNode, exponent: DecimalNode): DecimalNode

    /**
     * https://docs.jsonata.org/numeric-functions#random
     */
    fun random(): DecimalNode

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    fun round(number: DecimalNode, precision: DecimalNode? = null): DecimalNode

    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    fun sqrt(number: DecimalNode): DecimalNode

    // https://docs.jsonata.org/object-functions

    /**
     * https://docs.jsonata.org/object-functions#assert
     */
    @Throws(AssertionError::class)
    fun assert(condition: JsonNode, message: JsonNode): BooleanNode

    /**
     * https://docs.jsonata.org/object-functions#each
     */
    fun each(obj: ObjectNode, function: FunNode): ArrayNode

    /**
     * https://docs.jsonata.org/object-functions#error
     */
    @Throws(Error::class)
    fun error(message: JsonNode)

    /**
     * https://docs.jsonata.org/object-functions#keys
     */
    fun keys(arr: ArrayNode): ArrayNode

    /**
     * https://docs.jsonata.org/object-functions#keys
     */
    fun keys(obj: ObjectNode): ArrayNode

    /**
     * https://docs.jsonata.org/object-functions#lookup
     */
    fun lookup(array: ArrayNode, key: TextNode): JsonNode?

    /**
     * https://docs.jsonata.org/object-functions#lookup
     */
    fun lookup(obj: ObjectNode, key: TextNode): JsonNode?

    fun merge(array: ArrayNode): ObjectNode

    /**
     * https://docs.jsonata.org/object-functions#spread
     */
    fun spread(array: ArrayNode): ArrayNode

    /**
     * https://docs.jsonata.org/object-functions#spread
     */
    fun spread(obj: ObjectNode): ArrayNode

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    fun type(value: JsonNode?): TextNode


    // https://docs.jsonata.org/string-functions

    /**
     * https://docs.jsonata.org/string-functions#base64encode
     */
    fun base64decode(str: JsonNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#base64encode
     */
    fun base64encode(str: JsonNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    fun contains(str: JsonNode, pattern: JsonNode): BooleanNode

    /**
     * https://docs.jsonata.org/string-functions#decodeurl
     */
    fun decodeUrl(str: JsonNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#decodeurlcomponent
     */
    fun decodeUrlComponent(str: JsonNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#encodeurl
     */
    fun encodeUrl(str: JsonNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#encodeurlcomponent
     */
    fun encodeUrlComponent(str: JsonNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#eval
     */
    fun eval(expr: JsonNode, context: JsonNode? = null): JsonNode?

    /**
     * https://docs.jsonata.org/string-functions#join
     */
    fun join(array: JsonNode, separator: JsonNode?): TextNode

    /**
     * https://docs.jsonata.org/string-functions#length
     */
    fun length(str: JsonNode): DecimalNode

    /**
     * https://docs.jsonata.org/string-functions#lowercase
     */
    fun lowercase(str: JsonNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#join
     */
    fun match(str: JsonNode, pattern: JsonNode, limit: JsonNode?): ArrayNode

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    fun pad(str: JsonNode, width: JsonNode, char: JsonNode?): TextNode

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    fun replace(str: JsonNode, pattern: JsonNode, replacement: JsonNode, limit: JsonNode?): TextNode

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    fun split(str: JsonNode, separator: JsonNode, limit: JsonNode?): ArrayNode

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    fun string(arg: JsonNode?, prettify: BooleanNode? = null): TextNode

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    fun substring(str: JsonNode, start: DecimalNode, length: DecimalNode? = null): TextNode

    /**
     * https://docs.jsonata.org/string-functions#substringafter
     */
    fun substringAfter(str: JsonNode, chars: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#substringbefore
     */
    fun substringBefore(str: JsonNode, chars: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#trim
     */
    fun trim(str: JsonNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#uppercase
     */
    fun uppercase(str: JsonNode): TextNode

}

