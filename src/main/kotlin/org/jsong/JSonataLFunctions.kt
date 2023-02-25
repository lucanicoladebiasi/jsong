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
import com.fasterxml.jackson.databind.node.*

/**
 * This interface defines the built-in functions library defined by the JSONata language.
 *
 * @see Library
 */
interface JSonataLFunctions {

    // https://docs.jsonata.org/array-functions

    /**
     *
     * Returns an array containing the values in [array1] followed by the values in [array2].
     * If either parameter is not an array, then it is treated as a singleton array containing that value.
     *
     * @see [append](https://docs.jsonata.org/array-functions#append)
     */
    fun append(array1: JsonNode, array2: JsonNode): ArrayNode

    /**
     * Returns the number of items in the [array] parameter.
     * If the [array] parameter is not an array, but rather a value of another JSON type,
     * then the parameter is treated as a singleton array containing that value, and this function returns `1`.
     *
     * If array is not specified, then the context value is used as the value of array.
     *
     * @see [count](https://docs.jsonata.org/array-functions#count)
     */
    fun count(array: JsonNode): DecimalNode

    /**
     * Returns an [array] containing all the values from the array parameter,
     * but with any duplicates removed.
     *
     * Values are tested for deep equality as if by using the
     * [equality](https://docs.jsonata.org/comparison-operators#-equals) operator.
     *
     * @see [distinct](]https://docs.jsonata.org/array-functions#distinct)
     */
    fun distinct(array: JsonNode): ArrayNode

    /**
     * Returns an [array] containing all the values from the array parameter, but in reverse order.
     *
     * @see [reverse](https://docs.jsonata.org/array-functions#reverse)
     */
    fun reverse(array: JsonNode): ArrayNode

    /**
     * Returns an array containing all the values from the [array] parameter,
     * but shuffled into random order.
     *
     * @see [shuffle](https://docs.jsonata.org/array-functions#shuffle)
     */
    fun shuffle(array: JsonNode): ArrayNode

    /**
     * Returns an array containing all the values in the [array] parameter, but sorted into order.
     * If no function parameter is supplied, then the array parameter must contain only numbers or only strings,
     * and they will be sorted in order of increasing number, or increasing unicode codepoint respectively.
     *
     * If a comparator [function] is supplied, then is must be a function that takes two parameters:
     *
     * `function(left, right)`
     *
     * This function gets invoked by the sorting algorithm to compare two values left and right.
     * If the value of left should be placed after the value of right in the desired sort order,
     * then the function must return Boolean `true` to indicate a swap.
     * Otherwise, it must return `false`.
     *
     * @param array to sort.
     * @param function sorting. `null` by default, it sorts numbers and strings in ascending order.
     *
     * @see [sort](https://docs.jsonata.org/array-functions#sort)
     *
     */
    fun sort(array: JsonNode, function: FunNode? = null): ArrayNode

    /**
     * Returns a convolved (zipped) array containing grouped [arrays] of values from the `array1 ... arrayN`
     * arguments from index 0, 1, 2, etc.
     *
     * This function accepts a variable number of arguments.
     * The length of the returned array is equal to the length of the shortest array in the arguments.
     *
     * @see [zip](https://docs.jsonata.org/array-functions#zip)
     */
    fun zip(vararg arrays: JsonNode): ArrayNode

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
    fun max(array: JsonNode): DecimalNode

    /**
     * https://docs.jsonata.org/aggregation-functions#min
     */
    fun min(array: JsonNode): DecimalNode

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
    fun formatNumber(number: DecimalNode, picture: TextNode, options: TextNode? = null): TextNode

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
    fun keys(array: ArrayNode): ArrayNode

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
    fun join(array: JsonNode, separator: JsonNode? = null): TextNode

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
    fun match(str: JsonNode, pattern: JsonNode, limit: JsonNode? = null): ArrayNode

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    fun pad(str: JsonNode, width: JsonNode, char: JsonNode? = null): TextNode

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    fun replace(str: JsonNode, pattern: JsonNode, replacement: JsonNode, limit: JsonNode? = null): TextNode

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    fun split(str: JsonNode, separator: JsonNode, limit: JsonNode? = null): ArrayNode

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

