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
interface JSonataFunctions {

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
     * Casts the argument to a Boolean using the following rules:
     *
     * * Argument type	-> Result
     *      * Boolean -> unchanged
     *      * string: empty	-> `false`
     *      * string: non-empty	-> `true`
     *      * number: 0	-> `false`
     *      * number: non-zero	-> `true`
     *      * `null` -> `false`
     *      * array: empty	-> `false`
     *      * array: contains a member that casts to `true` -> `true`
     *      * array: all members cast to `false` ->	`false`
     *      * object: empty	-> `false`
     *      * object: non-empty	-> `true`
     *      * function	-> `false`
     *
     * @see [boolean](https://docs.jsonata.org/boolean-functions#boolean)
     */
    fun boolean(arg: JsonNode?): BooleanNode

    /**
     * Returns boolean NOT on the [arg].
     *
     * @param arg is first cast to a boolean.
     *
     * @see [not](https://docs.jsonata.org/boolean-functions#not)
     */
    fun not(arg: BooleanNode): BooleanNode

    /**
     * Returns `true` if the [arg] expression evaluates to a value,
     * or `false` if the expression does not match anything (e.g. a path to a non-existent field reference).
     *
     * @see [exists](https://docs.jsonata.org/boolean-functions#exists)
     */
    fun exists(arg: JsonNode?): BooleanNode

    // https://docs.jsonata.org/date-time-functions

    /**
     * Convert the [number] representing milliseconds since the Unix Epoch (1 January, 1970 UTC)
     * to a formatted string representation of the timestamp as specified by the picture string.
     *
     * @param picture   If omitted, then the timestamp is formatted in the ISO 8601 format.
     *                  If the optional string is supplied, then the timestamp is formatted
     *                  occording to the representation specified in that string.
     *
     * @param timezone  If supplied, then the formatted timestamp will be in that timezone.
     *                  The timezone string should be in the format `±HHMM`,
     *                  where `±` is either the plus or minus sign and `HHMM`
     *                  is the offset in hours and minutes from UTC.
     *                  Positive offset for timezones east of UTC, negative offset for timezones west of UTC.
     *
     * @see [fromMillis]](https://docs.jsonata.org/date-time-functions#frommillis)
     */
    fun fromMillis(number: DecimalNode, picture: TextNode? = null, timezone: TextNode? = null): TextNode

    /**
     * @see [millis](https://docs.jsonata.org/date-time-functions#millis)
     */
    fun millis(): DecimalNode

    /**
     * Generates a UTC timestamp in ISO 8601 compatible format and returns it as a string.
     * All invocations of $now() within an evaluation of an expression will all return the same timestamp value.
     *
     * @param picture   If supplied, then the current timestamp is formatted as described by the [fromMillis] function.
     *
     * @see [now](https://docs.jsonata.org/date-time-functions#now)
     */
    fun now(picture: TextNode? = null, timezone: TextNode? = null): TextNode

    /**
     * Convert a timestamp string to the number of milliseconds since the Unix Epoch (1 January, 1970 UTC) as a number.
     *
     * @param timestamp If not specified, then the format of the timestamp is assumed to be ISO 8601.
     *                  An error is thrown if the string is not in the correct format.
     *
     * @param picture   If specified, then the format is assumed to be described by this picture string using the
     *                  [same syntax as the XPath/XQuery function](https://www.w3.org/TR/xpath-functions-31/#date-picture-string) specification.
     *
     * @see [toMillis](https://docs.jsonata.org/date-time-functions#tomillis)
     */
    fun toMillis(timestamp: TextNode, picture: TextNode? = null): DecimalNode


    // https://docs.jsonata.org/higher-order-functions

    /**
     * Returns an array containing only the values in the [array] parameter that satisfy the [function] predicate
     * (i.e. function returns Boolean true when passed the value).
     *
     * The function that is supplied as the second parameter must have the following signature:
     *
     * `function(value [, index [, array]])`
     *
     * Each value in the input array is passed in as the first parameter in the supplied function.
     * The index (position) of that value in the input array is passed in as the second parameter, if specified.
     * The whole input array is passed in as the third parameter, if specified.
     *
     * @see [filter](https://docs.jsonata.org/higher-order-functions#filter)
     */
    fun filter(array: ArrayNode, function: FunNode): ArrayNode

    /**
     * Returns an array containing the results of applying the [function] parameter to each value in the [array]
     * parameter.
     *
     * The function that is supplied as the second parameter must have the following signature:
     *
     * function(value [, index [, array]])
     *
     * Each value in the input array is passed in as the first parameter in the supplied function.
     * The index (position) of that value in the input array is passed in as the second parameter, if specified.
     * The whole input array is passed in as the third parameter, if specified.
     *
     * @see [map](https://docs.jsonata.org/higher-order-functions#map)
     */
    fun map(array: ArrayNode, function: FunNode): ArrayNode

    /**
     * Returns an aggregated value derived from applying the [function] parameter successively
     * to each value in [array] in combination with the result of the previous application of the function.
     *
     * The function must accept at least two arguments,
     * and behaves like an infix operator between each value within the array.
     * The signature of this supplied function must be of the form:
     *
     * `myfunc($accumulator, $value[, $index[, $array]])`
     *
     * @see [reduce](https://docs.jsonata.org/higher-order-functions#reduce)
     */
    fun reduce(array: ArrayNode, function: FunNode, init: FunNode): JsonNode

    /**
     * Returns an object that contains only the key/value pairs from the [obj] parameter
     * that satisfy the predicate function passed in as the second parameter.
     *
     * If [obj] is not specified, then the context value is used as the value of object.
     * It is an error if object is not an object.
     *
     * The [function] that is supplied as the second parameter must have the following signature:
     *
     * `function(value [, key [, object]])`
     *
     * Each value in the input object is passed in as the first parameter in the supplied function.
     * The key (property name) of that value in the input object is passed in as the second parameter, if specified.
     * The whole input object is passed in as the third parameter, if specified.
     *
     * @see [sift](https://docs.jsonata.org/higher-order-functions#reduce)
     */
    fun sift(obj: ObjectNode, function: FunNode): JsonNode

    /**
     * Returns the one and only one value in the [array] parameter that satisfy the function predicate
     * (i.e. function returns Boolean true when passed the value).
     * Throws an exception if the number of matching values is not exactly one.
     *
     * The function that is supplied as the second parameter must have the following signature:
     *
     * `function(value [, index [, array]])`
     *
     * Each value in the input array is passed in as the first parameter in the supplied function.
     * The index (position) of that value in the input array is passed in as the second parameter, if specified.
     * The whole input array is passed in as the third parameter, if specified.
     *
     * @see [single](https://docs.jsonata.org/higher-order-functions#single)
     */
    fun single(array: ArrayNode, function: FunNode): JsonNode


    // https://docs.jsonata.org/aggregation-functions

    /**
     * Returns the mean value of an [array] of numbers.
     *
     * It is an error if the input array contains an item which isn't a number.
     *
     * @see [average](https://docs.jsonata.org/aggregation-functions#average)
     */
    fun average(array: JsonNode): DecimalNode

    /**
     * Returns the maximum number in an [array] of numbers.
     *
     * It is an error if the input array contains an item which isn't a number.
     *
     * @see [max](https://docs.jsonata.org/aggregation-functions#max)
     */
    fun max(array: JsonNode): DecimalNode

    /**
     * Returns the minimum number in an [array] of numbers.
     *
     * It is an error if the input array contains an item which isn't a number.
     *
     * @see [min](https://docs.jsonata.org/aggregation-functions#min)
     */
    fun min(array: JsonNode): DecimalNode

    /**
     * Returns the arithmetic sum of an [array] of numbers.
     *
     * It is an error if the input array contains an item which isn't a number.
     *
     * https://docs.jsonata.org/aggregation-functions#sum
     */
    fun sum(array: JsonNode): DecimalNode


    // https://docs.jsonata.org/numeric-functions

    /**
     * Returns the absolute value of the [number] parameter,
     * i.e. if the number is negative, it returns the positive value.
     *
     * If [number] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of number.
     *
     * @see [abs]](https://docs.jsonata.org/numeric-functions#abs)
     */
    fun abs(number: DecimalNode): DecimalNode

    /**
     * Returns the value of [number] rounded up to the nearest integer that is greater than or equal to number.
     *
     * If [number] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of number.
     *
     * @see [ceil](https://docs.jsonata.org/numeric-functions#ceil)
     */
    fun ceil(number: DecimalNode): DecimalNode

    /**
     * Returns the value of [number] rounded down to the nearest integer that is smaller or equal to number.
     *
     * If [number] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of number.
     *
     * @see [floor](https://docs.jsonata.org/numeric-functions#floor)
     */
    fun floor(number: DecimalNode): DecimalNode

    /**
     * Casts the [number] to a string and
     * formats it to an integer represented in the number base specified by the [radix] argument.
     *
     * If [radix] is not specified, then it defaults to base 10.
     * The [radix] parameter can be between 2 and 36, otherwise an error is thrown.
     *
     * @see [formatBase](https://docs.jsonata.org/numeric-functions#formatbase)
     */
    fun formatBase(number: DecimalNode, radix: DecimalNode? = null): TextNode

    /**
     * Casts the [number] to a string and formats it to an integer representation as specified by the picture string.
     *
     * The behaviour of this function is consistent with the two-argument version of the
     * [XPath/XQuery function fn:format-integer](https://www.w3.org/TR/xpath-functions-31/#func-format-integer)
     * as defined in the XPath F&O 3.1 specification.
     *
     * The [picture] string parameter defines how the number is formatted and
     * has the same syntax as `fn:format-integer`.
     *
     * @see [formatInteger](https://docs.jsonata.org/numeric-functions#formatinteger)
     */
    fun formatInteger(number: DecimalNode, picture: TextNode): TextNode

    /**
     * Casts the [number] to a string and formats it to a decimal representation as specified by the picture string.
     *
     * The behaviour of this function is consistent with the
     * [XPath/XQuery function fn:format-number](https://www.w3.org/TR/xpath-functions-31/#func-format-number)
     * as defined in the XPath F&O 3.1 specification.
     *
     * The [picture] string parameter defines how the number is formatted and has the
     * [same syntax](https://www.w3.org/TR/xpath-functions-31/#syntax-of-picture-string)
     * as `fn:format-number`.
     *
     * The optional third argument [options] is used to override the default locale specific formatting characters
     * such as the decimal separator.
     * If supplied, this argument must be an object containing name/value pairs
     * specified in the [decimal format](https://www.w3.org/TR/xpath-functions-31/#defining-decimal-format)
     * section of the XPath F&O 3.1 specification.
     *
     * @see [formatNumber](https://docs.jsonata.org/numeric-functions#formatnumber)
     */
    fun formatNumber(number: DecimalNode, picture: TextNode, options: TextNode? = null): TextNode

    /**
     * Casts the [arg] parameter to a number using the following casting rules
     * * Numbers are unchanged;
     * * Strings that contain a sequence of characters that represent a legal JSON number are converted to that number;
     * * Hexadecimal numbers start with 0x, Octal numbers with 0o, binary numbers with 0b
     * * Boolean `true` casts to 1, Boolean `false` casts to 0
     * * All other values cause an error to be thrown.
     *
     * If [arg] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of [arg].
     *
     * @see [number](https://docs.jsonata.org/numeric-functions#number)
     */
    fun number(arg: JsonNode?): DecimalNode

    /**
     * Parses the contents of the [string] parameter to an integer (as a JSON number)
     * using the format specified by the picture string.
     *
     * The [picture] string parameter has the same format as $[formatInteger].
     * Although the XPath specification does not have an equivalent function for parsing integers,
     * this capability has been added to JSONata.
     *
     * @see [parseInteger](https://docs.jsonata.org/numeric-functions#random)
     */
    fun parseInteger(string: TextNode, picture: TextNode): DecimalNode

    /**
     * Returns the value of [base] raised to the [power] of exponent.
     *
     * If [base] is not specified (i.e. this function is invoked with one argument),
     * then the context value is used as the value of base.
     *
     * An error is thrown if the values of base and exponent lead to a value
     * that cannot be represented as a JSON number (e.g. Infinity, complex numbers)
     *
     * @see [power](https://docs.jsonata.org/numeric-functions#power)
     */
    fun power(base: DecimalNode, exponent: DecimalNode): DecimalNode

    /**
     * Returns a pseudo random number greater than or equal to zero and less than one (0 ≤ n < 1).
     *
     * @see [random](https://docs.jsonata.org/numeric-functions#random)
     */
    fun random(): DecimalNode

    /**
     * Returns the value of the [number] parameter rounded to
     * the number of decimal places specified by the optional [precision] parameter.
     *
     * The [precision] parameter (which must be an integer) species
     * the number of decimal places to be present in the rounded number.
     * * If [precision] is not specified then it defaults to the value `0`
     *   and the number is rounded to the nearest integer.
     * * If [precision] is negative,
     *   then its value specifies which column to round to on the left side of the decimal place.
     *
     * This function uses the [Round Half](https://en.wikipedia.org/wiki/Rounding#Round_half_to_even)
     * to even strategy to decide which way to round numbers that fall exactly between two candidates
     * at the specified precision.
     * This strategy is commonly used in financial calculations and is the default rounding mode in IEEE 754.
     *
     * @see [random](https://docs.jsonata.org/numeric-functions#round)
     */
    fun round(number: DecimalNode, precision: DecimalNode? = null): DecimalNode

    /**
     * Returns the square root of the value of the [number] parameter.
     *
     * If [number] is not specified (i.e. this function is invoked with one argument),
     * then the context value is used as the value of number.
     *
     * An error is thrown if the value of [number] is negative.
     *
     * @see [sqrt](https://docs.jsonata.org/numeric-functions#sqrt)
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

