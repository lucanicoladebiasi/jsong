package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*

interface JSonataLFunctions {

    // https://docs.jsonata.org/array-functions

//    fun append(array1: JsonNode?, array2: JsonNode?): ArrayNode
//
//    fun count(array: JsonNode?): DecimalNode
//
//    fun distinct(array: ArrayNode): ArrayNode
//
//    fun reverse(array: JsonNode): ArrayNode
//
//    fun shuffle(array: JsonNode): ArrayNode
//
//    fun sort(array: JsonNode?, function: FunNode?): ArrayNode
//
//    fun zip(vararg arrays: JsonNode): ArrayNode

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
    fun exists(arg: TextNode): BooleanNode

    // https://docs.jsonata.org/date-time-functions

//    fun fromMillis(number: JsonNode, picture: JsonNode?, timezone: JsonNode?)
//
//    fun millis(): DecimalNode
//
//    fun now(picture: JsonNode?, timezone: JsonNode?): TextNode
//
//    fun toMillis(timestamp: JsonNode, picture: JsonNode?)


    // https://docs.jsonata.org/higher-order-functions

//    fun filter(array: JsonNode, function: FunNode): ArrayNode
//
//    fun map(array: JsonNode, function: FunNode): ArrayNode
//
//    fun reduce(array: JsonNode, function: FunNode, init: FunNode): JsonNode
//
//    fun sift(obj: JsonNode, function: FunNode): JsonNode
//
//    fun single(array: JsonNode, function: FunNode): JsonNode

    // https://docs.jsonata.org/aggregation-functions

//    fun average(array: JsonNode?): DecimalNode
//
//    fun max(array: JsonNode?): DecimalNode
//
//    fun min(array: JsonNode?): DecimalNode
//
//    fun sum(array: JsonNode?): DecimalNode


    // https://docs.jsonata.org/numeric-functions

//    fun abs(number: JsonNode?): DecimalNode
//
//    fun ceil(number: JsonNode?): DecimalNode
//
//    fun floor(number: JsonNode?): DecimalNode
//
//    fun formatBase(number: JsonNode?, radix: DecimalNode?): TextNode
//
//    fun formatInteger(number: JsonNode?, picture: TextNode): TextNode
//
//    fun formatNumber(number: JsonNode?, picture: TextNode, options: TextNode?): TextNode
//
//    fun number(arg: JsonNode?): DecimalNode
//
//    fun parseInteger(string: JsonNode?, picture: TextNode): DecimalNode
//
//    fun power(base: JsonNode?, exponent: JsonNode?): DecimalNode
//
//    fun random(): DecimalNode
//
//    fun round(number: JsonNode?, precision: DecimalNode?): DecimalNode
//
//    fun sqrt(number: JsonNode?): DecimalNode

    // https://docs.jsonata.org/object-functions

//    fun assert(condition: JsonNode, message: JsonNode)
//
//    fun each(obj: ObjectNode, function: FunNode): ArrayNode
//
//    fun error(message: JsonNode)
//
//    fun keys(obj: ObjectNode): ArrayNode
//
//    fun lookup(obj: ObjectNode, key: JsonNode): JsonNode?
//
//    fun merge(array: ArrayNode): ObjectNode
//
//    fun spread(obj: ObjectNode): ArrayNode
//
//    fun type(value: JsonNode): TextNode

    // https://docs.jsonata.org/string-functions

    /**
     * https://docs.jsonata.org/string-functions#base64encode
     */
    fun base64decode(str: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#base64encode
     */
    fun base64encode(str: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    fun contains(str: TextNode, pattern: RegexNode): BooleanNode

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    fun contains(str: TextNode, pattern: TextNode): BooleanNode


    /**
     * https://docs.jsonata.org/string-functions#decodeurl
     */
    fun decodeUrl(str: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#decodeurlcomponent
     */
    fun decodeUrlComponent(str: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#encodeurl
     */
    fun encodeUrl(str: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#encodeurlcomponent
     */
    fun encodeUrlComponent(str: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#eval
     */
    fun eval(expr: TextNode, context: JsonNode? = null): JsonNode?

    /**
     * https://docs.jsonata.org/string-functions#join
     */
    fun join(array: ArrayNode, separator: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#length
     */
    fun length(str: TextNode): DecimalNode

    /**
     * https://docs.jsonata.org/string-functions#lowercase
     */
    fun lowercase(str: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#join
     */
    fun match(str: JsonNode, pattern: RegexNode, limit: DecimalNode? = null)

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    fun pad(str: TextNode, width: DecimalNode, char: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    fun replace(str: TextNode, pattern: RegexNode, replacement: TextNode, limit: DecimalNode? = null): TextNode

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    fun replace(str: TextNode, pattern: TextNode, replacement: TextNode, limit: DecimalNode? = null): TextNode

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    fun split(str: TextNode, separator: RegexNode, limit: DecimalNode?): ArrayNode

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    fun split(str: TextNode, separator: TextNode, limit: DecimalNode?): ArrayNode

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    fun string(arg: JsonNode?, prettify: BooleanNode? = null): TextNode

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    fun substring(str: TextNode, start: DecimalNode, length: DecimalNode? = null): TextNode

    /**
     * https://docs.jsonata.org/string-functions#substringafter
     */
    fun substringAfter(str: TextNode, chars: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#substringbefore
     */
    fun substringBefore(str: TextNode, chars: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#trim
     */
    fun trim(str: TextNode): TextNode

    /**
     * https://docs.jsonata.org/string-functions#uppercase
     */
    fun uppercase(str: TextNode): TextNode


}

