/**
 * MIT License
 *
 * Copyright (c) 2023 Luca Nicola Debiasi
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.lucanicoladebiasi.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*

/**
 * This interface defines the built-in functions library defined by the JSONata language.
 *
 * The documentation is derived from the JSONata Function Library
 * * [Array functions](https://docs.jsonata.org/array-functions)
 * * [Boolean functions](https://docs.jsonata.org/boolean-functions)
 * * [Date/Time functions](https://docs.jsonata.org/date-time-functions)
 * * [Higher order functions](https://docs.jsonata.org/higher-order-functions)
 * * [Numeric functions](https://docs.jsonata.org/numeric-functions)
 * * [Numeric aggregation functions](https://docs.jsonata.org/aggregation-functions)
 * * [Object functions](https://docs.jsonata.org/object-functions)
 * * [String functions ](https://docs.jsonata.org/string-functions)
 *
 * Implementation of this interface can differ from the official JSON specifications described here:
 * see the documentation of the implementations for specific differences.
 *
 * @see Library
 */
interface JSONataFunctionLibrary {

    // https://docs.jsonata.org/array-functions

    /**
     * Returns an array containing the values in [array1] followed by the values in [array2].
     * If either parameter is not an array, then it is treated as a singleton array containing that value.
     *
     * See [append](https://docs.jsonata.org/array-functions#append)
     */
    fun append(array1: JsonNode, array2: JsonNode): ArrayNode

    /**
     * Returns the number of items in the [array] parameter.
     * If the [array] parameter is not an array, but rather a value of another JSON type,
     * then the parameter is treated as a singleton array containing that value, and this function returns `1`.
     *
     * If array is not specified, then the context value is used as the value of array.
     *
     * See [count](https://docs.jsonata.org/array-functions#count)
     */
    fun count(array: JsonNode): DecimalNode

    /**
     * Returns an [array] containing all the values from the array parameter,
     * but with any duplicates removed.
     *
     * Values are tested for deep equality as if by using the
     * [equality](https://docs.jsonata.org/comparison-operators#-equals) operator.
     *
     * See [distinct](]https://docs.jsonata.org/array-functions#distinct)
     */
    fun distinct(array: JsonNode): ArrayNode

    /**
     * Returns an [array] containing all the values from the array parameter, but in reverse order.
     *
     * See [reverse](https://docs.jsonata.org/array-functions#reverse)
     */
    fun reverse(array: JsonNode): ArrayNode

    /**
     * Returns an array containing all the values from the [array] parameter,
     * but shuffled into random order.
     *
     * See [shuffle](https://docs.jsonata.org/array-functions#shuffle)
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
     * See [sort](https://docs.jsonata.org/array-functions#sort)
     *
     */
    fun sort(array: JsonNode, function: JsonNode? = null): ArrayNode

    /**
     * Returns a convolved (zipped) array containing grouped [arrays] of values from the `array1 ... arrayN`
     * arguments from index 0, 1, 2, etc.
     *
     * This function accepts a variable number of arguments.
     * The length of the returned array is equal to the length of the shortest array in the arguments.
     *
     * See [zip](https://docs.jsonata.org/array-functions#zip)
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
     * See [boolean](https://docs.jsonata.org/boolean-functions#boolean)
     */
    fun boolean(arg: JsonNode?): BooleanNode

    /**
     * Returns boolean NOT on the [arg].
     *
     * @param arg is first cast to a boolean.
     *
     * See [not](https://docs.jsonata.org/boolean-functions#not)
     */
    fun not(arg: BooleanNode): BooleanNode

    /**
     * Returns `true` if the [arg] expression evaluates to a value,
     * or `false` if the expression does not match anything (e.g. a path to a non-existent field reference).
     *
     * See [exists](https://docs.jsonata.org/boolean-functions#exists)
     */
    fun exists(arg: JsonNode?): BooleanNode

    // https://docs.jsonata.org/date-time-functions

    /**
     * Convert the [number] representing milliseconds since the Unix Epoch (1 January 1970 UTC)
     * to a formatted string representation of the timestamp as specified by the picture string.
     *
     * @param picture   If omitted, then the timestamp is formatted in the ISO 8601 format.
     *                  If the optional string is supplied, then the timestamp is formatted
     *                  according to the representation specified in that string.
     *
     * @param timezone  If supplied, then the formatted timestamp will be in that timezone.
     *                  The timezone string should be in the format `±HHMM`,
     *                  where `±` is either the plus or minus sign and `HHMM`
     *                  is the offset in hours and minutes from UTC.
     *                  Positive offset for timezones east of UTC, negative offset for timezones west of UTC.
     *
     * See [fromMillis]](https://docs.jsonata.org/date-time-functions#frommillis)
     */
    fun fromMillis(number: DecimalNode, picture: TextNode? = null, timezone: TextNode? = null): TextNode

    /**
     * See [millis](https://docs.jsonata.org/date-time-functions#millis)
     */
    fun millis(): DecimalNode

    /**
     * Generates a UTC timestamp in ISO 8601 compatible format and returns it as a string.
     * All invocations of $now() within an evaluation of an expression will all return the same timestamp value.
     *
     * @param picture   If supplied, then the current timestamp is formatted as described by the [fromMillis] function.
     *
     * See [now](https://docs.jsonata.org/date-time-functions#now)
     */
    fun now(picture: TextNode? = null, timezone: TextNode? = null): TextNode

    /**
     * Convert a timestamp string to the number of milliseconds since the Unix Epoch (1 January 1970 UTC) as a number.
     *
     * @param timestamp If not specified, then the format of the timestamp is assumed to be ISO 8601.
     *                  An error is thrown if the string is not in the correct format.
     *
     * @param picture   If specified, then the format is assumed to be described by this picture string using the
     *                  [same syntax as the XPath/XQuery function](https://www.w3.org/TR/xpath-functions-31/#date-picture-string) specification.
     *
     * See [toMillis](https://docs.jsonata.org/date-time-functions#tomillis)
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
     * See [filter](https://docs.jsonata.org/higher-order-functions#filter)
     */
    fun filter(array: ArrayNode, function: FunctionNode): ArrayNode

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
     * See [map](https://docs.jsonata.org/higher-order-functions#map)
     */
    fun map(array: ArrayNode, function: FunctionNode): ArrayNode

    /**
     * Returns an aggregated value derived from applying the [function] parameter successively
     * to each value in [array] in combination with the result of the previous application of the function.
     *
     * The function must accept at least two arguments,
     * and behaves like an infix operator between each value within the array.
     * The signature of this supplied function must be of the form:
     *
     * `func($accumulator, $value[, $index[, $array]])`
     *
     * See [reduce](https://docs.jsonata.org/higher-order-functions#reduce)
     */
    fun reduce(array: ArrayNode, function: FunctionNode, init: FunctionNode): JsonNode

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
     * See [sift](https://docs.jsonata.org/higher-order-functions#reduce)
     */
    fun sift(obj: ObjectNode, function: FunctionNode): JsonNode

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
     * See [single](https://docs.jsonata.org/higher-order-functions#single)
     */
    fun single(array: ArrayNode, function: FunctionNode): JsonNode


    // https://docs.jsonata.org/aggregation-functions

    /**
     * Returns the mean value of an [array] of numbers.
     *
     * It is an error if the input array contains an item which isn't a number.
     *
     * See [average](https://docs.jsonata.org/aggregation-functions#average)
     */
    fun average(array: JsonNode): DecimalNode

    /**
     * Returns the maximum number in an [array] of numbers.
     *
     * It is an error if the input array contains an item which isn't a number.
     *
     * See [max](https://docs.jsonata.org/aggregation-functions#max)
     */
    fun max(array: JsonNode): DecimalNode

    /**
     * Returns the minimum number in an [array] of numbers.
     *
     * It is an error if the input array contains an item which isn't a number.
     *
     * See [min](https://docs.jsonata.org/aggregation-functions#min)
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
     * See [abs]](https://docs.jsonata.org/numeric-functions#abs)
     */
    fun abs(number: DecimalNode): DecimalNode

    /**
     * Returns the value of [number] rounded up to the nearest integer that is greater than or equal to number.
     *
     * If [number] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of number.
     *
     * See [ceil](https://docs.jsonata.org/numeric-functions#ceil)
     */
    fun ceil(number: DecimalNode): DecimalNode

    /**
     * Returns the value of [number] rounded down to the nearest integer that is smaller or equal to number.
     *
     * If [number] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of number.
     *
     * See [floor](https://docs.jsonata.org/numeric-functions#floor)
     */
    fun floor(number: DecimalNode): DecimalNode

    /**
     * Casts the [number] to a string and
     * formats it to an integer represented in the number base specified by the [radix] argument.
     *
     * If [radix] is not specified, then it defaults to base 10.
     * The [radix] parameter can be between 2 and 36, otherwise an error is thrown.
     *
     * See [formatBase](https://docs.jsonata.org/numeric-functions#formatbase)
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
     * See [formatInteger](https://docs.jsonata.org/numeric-functions#formatinteger)
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
     * See [formatNumber](https://docs.jsonata.org/numeric-functions#formatnumber)
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
     * See [number](https://docs.jsonata.org/numeric-functions#number)
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
     * See [parseInteger](https://docs.jsonata.org/numeric-functions#random)
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
     * See [power](https://docs.jsonata.org/numeric-functions#power)
     */
    fun power(base: DecimalNode, exponent: DecimalNode): DecimalNode

    /**
     * Returns a pseudo random number greater than or equal to zero and less than one (0 ≤ n < 1).
     *
     * See [random](https://docs.jsonata.org/numeric-functions#random)
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
     * See [random](https://docs.jsonata.org/numeric-functions#round)
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
     * See [sqrt](https://docs.jsonata.org/numeric-functions#sqrt)
     */
    fun sqrt(number: DecimalNode): DecimalNode


    // https://docs.jsonata.org/object-functions

    /**
     * If [condition] is `true`, the function returns undefined.
     * If the [condition] is `false`, an exception is thrown with the [message] as the message of the exception.
     *
     * See [assert](https://docs.jsonata.org/object-functions#assert)
     */
    @Throws(AssertionError::class)
    fun assert(condition: JsonNode, message: JsonNode): BooleanNode

    /**
     * Returns an array containing the values return by the function when applied to each key/value pair in the [obj].
     *
     * The [function] parameter will get invoked with two arguments:
     *
     * `function(value, name)`
     *
     * where the value `parameter` is the value of each name/value pair in the object and name is its name.
     * The `name` parameter is optional.
     *
     * See [each](https://docs.jsonata.org/object-functions#each)
     */
    fun each(obj: ObjectNode, function: FunctionNode): ArrayNode

    /**
     * Deliberately throws an error with an optional [message].
     *
     * See [error]https://docs.jsonata.org/object-functions#error
     */
    @Throws(Error::class)
    fun error(message: JsonNode?)

    /**
     * Returns an array containing a de-duplicated list of all the keys in all the objects of the [array].
     *
     * See [keys](https://docs.jsonata.org/object-functions#keys)
     */
    fun keys(array: ArrayNode): ArrayNode

    /**
     * Returns an array containing the keys in the [obj].
     *
     * See [keys](https://docs.jsonata.org/object-functions#keys)
     */
    fun keys(obj: ObjectNode): ArrayNode

    /**
     * Search all the objects part of the [array] returning all the values associated with all occurrences of [key].
     *
     * See [lookup](https://docs.jsonata.org/object-functions#lookup)
     */
    fun lookup(array: ArrayNode, key: TextNode): JsonNode?

    /**
     * Returns the value associated with key in [obj].
     *
     * See [lookup](https://docs.jsonata.org/object-functions#lookup)
     */
    fun lookup(obj: ObjectNode, key: TextNode): JsonNode?

    /**
     * Merges an [array] of objects into a single object containing
     * all the key/value pairs from each of the objects in the input array.
     * If any of the input objects contain the same key,
     * then the returned object will contain the value of the last one in the array.
     *
     * It is an error if the input array contains an item that is not an object.
     *
     * See [merge](https://docs.jsonata.org/object-functions#merge)
     */
    fun merge(array: ArrayNode): ObjectNode

    /**
     * Return an array containing an object for every key/value pair in every object in the supplied [array].
     *
     * See [spread](https://docs.jsonata.org/object-functions#spread)
     */
    fun spread(array: ArrayNode): ArrayNode

    /**
     * Splits an object containing key/value pairs into an array of objects,
     * each of which has a single key/value pair from the input [obj].
     *
     * See [spread](https://docs.jsonata.org/object-functions#spread)
     */
    fun spread(obj: ObjectNode): ArrayNode

    /**
     * Evaluates the type of [value] and returns one of the following strings:
     * * `null`
     * * `number`
     * * `string`
     * * `boolean`
     * * `array`
     * * `object`
     * * `function`
     *
     * Returns `undefined` when value is undefined/not recognized.
     *
     * See [type](https://docs.jsonata.org/object-functions#type)
     */
    fun type(value: JsonNode?): TextNode


    // https://docs.jsonata.org/string-functions

    /**
     * Converts base 64 encoded bytes to a string, using a UTF-8 Unicode codepage.
     *
     * @param str base 64 encoded bytes.
     *
     * See [base64decode](https://docs.jsonata.org/string-functions#base64decode)
     */
    fun base64decode(str: JsonNode): TextNode

    /**
     * Converts an ASCII string to a base 64 representation.
     *
     * Each character in the [str] is treated as a byte of binary data.
     * This requires that all characters in the string are in the `0x00` to `0xFF` range,
     * which includes all characters in URI encoded strings.
     *
     * Unicode's characters outside of that range are not supported.
     * https://docs.jsonata.org/string-functions#base64encode
     *
     * See [base64encode](https://docs.jsonata.org/string-functions#base64encode)
     */
    fun base64encode(str: JsonNode): TextNode

    /**
     * Returns `true` if [str] is matched by pattern,
     * otherwise it returns `false`.
     *
     * If [str] is not specified (i.e. this function is invoked with one argument),
     * then the context value is used as the value of [str].
     *
     * The [pattern] parameter can either be a string or a regular expression (regex).
     * * If it is a string, the function returns `true`
     *   if the characters within pattern are contained contiguously within str.
     * * If it is a regex, the function will return true if the regex matches the contents of [str].
     *
     * See [contains](https://docs.jsonata.org/string-functions#contains)
     */
    fun contains(str: JsonNode, pattern: JsonNode): BooleanNode

    /**
     * Decodes a Uniform Resource Locator (URL) previously created by [encodeUrl].
     *
     * @param str content to decode.
     *
     * See [decodeUrl](https://docs.jsonata.org/string-functions#decodeurl)
     */
    fun decodeUrl(str: JsonNode): TextNode

    /**
     * Decodes a Uniform Resource Locator (URL) component previously created by [encodeUrlComponent].
     *
     * @param str content to decode.
     *
     * See [decodeUrlComponent](https://docs.jsonata.org/string-functions#decodeurlcomponent)
     */
    fun decodeUrlComponent(str: JsonNode): TextNode

    /**
     * Encodes a Uniform Resource Locator (URL) by replacing each instance of certain characters
     * by one, two, three, or four escape sequences representing the UTF-8 encoding of the character.
     *
     * @param str content to encode.
     *
     * See [encodeUrl](https://docs.jsonata.org/string-functions#encodeurl)
     */
    fun encodeUrl(str: JsonNode): TextNode

    /**
     * Encodes a Uniform Resource Locator (URL) component by replacing each instance of certain characters
     * by one, two, three, or four escape sequences representing the UTF-8 encoding of the character.
     *
     * @param str content to encode.
     *
     * See [encodeUrlComponent](https://docs.jsonata.org/string-functions#encodeurlcomponent)
     */
    fun encodeUrlComponent(str: JsonNode): TextNode

    /**
     * Parses and evaluates the string [expr] which contains literal JSON or a JSONata expression
     * using the current context as the context for evaluation.
     *
     * See [eval](https://docs.jsonata.org/string-functions#eval)
     */
    fun eval(expr: JsonNode, context: JsonNode? = null): JsonNode?

    /**
     * Joins an [array] of component strings into a single concatenated string
     * with each component string separated by the optional [separator] parameter.
     *
     * It is an error if the input [array] contains an item which isn't a string.
     *
     * If [separator] is not specified, then it is assumed to be the empty string,
     * i.e. no separator between the component strings.
     * It is an error if separator is not a string.
     *
     * See [join](https://docs.jsonata.org/string-functions#join)
     */
    fun join(array: JsonNode, separator: JsonNode? = null): TextNode

    /**
     * Returns the number of characters in the string [str].
     *
     * If [str] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of [str].
     *
     * An error is thrown if [str] is not a string.
     *
     * See [length](https://docs.jsonata.org/string-functions#length)
     */
    fun length(str: JsonNode): DecimalNode

    /**
     * Returns a string with all the characters of str converted to lowercase from [str] content.
     *
     * If [str] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of [str].
     *
     * An error is thrown if [str] is not a string.
     *
     * See [lowercase](https://docs.jsonata.org/string-functions#lowercase)
     */
    fun lowercase(str: JsonNode): TextNode

    /**
     * Applies the [str] string to the [pattern] regular expression and returns an array of objects,
     * with each object containing information about each occurrence of a match withing [str].
     *
     * The object contains the following fields:
     * * `match` - the substring that was matched by the regex.
     * * `index` - the offset (starting at zero) within str of this match.
     * * `groups` - if the regex contains capturing groups (parentheses),
     *    this contains an array of strings representing each captured group.
     *
     * If [str] is not specified, then the context value is used as the value of [str].
     *
     * It is an error if [str] is not a string.
     *
     * See [match](https://docs.jsonata.org/string-functions#join)
     */
    fun match(str: JsonNode, pattern: JsonNode, limit: JsonNode? = null): ArrayNode

    /**
     * Returns a copy of the string [str] with extra padding,
     * if necessary,
     * so that its total number of characters is at least the absolute value of the [width] parameter.
     *
     * If [width] is a positive number, then the string is padded to the right;
     * if negative, it is padded to the left.
     *
     * The optional [char] argument specifies the padding character(s) to use.
     * If not specified, it defaults to the space character.
     *
     * See [pad](https://docs.jsonata.org/string-functions#pad)
     */
    fun pad(str: JsonNode, width: JsonNode, char: JsonNode? = null): TextNode

    /**
     * Finds occurrences of pattern within [str] and replaces them with [replacement].
     *
     * If [str] is not specified, then the context value is used as the value of [str].
     *
     * It is an error if [str] is not a string.
     *
     * The [pattern] parameter can either be a string or a regular expression (regex).
     * * If it is a string, it specifies the substring(s) within str which should be replaced.
     * * If it is a regex, its is used to find.
     *
     * The [replacement] parameter can either be a string or a function.
     * * If it is a string, it specifies the sequence of characters
     *   that replace the substring(s) that are matched by pattern.
     * * If pattern is a regex,
     *   then the replacement string can refer to the characters that were matched by the regex
     *   as well as any of the captured groups using a `$` followed by a number `N`:
     *      * If `N = 0`, then it is replaced by substring matched by the regex as a whole.
     *      * If `N > 0`, then it is replaced by the substring captured by the Nth parenthesised group in the regex.
     *      * If `N` is greater than the number of captured groups, then it is replaced by the empty string.
     *      * A literal `$` character must be written as `$$` in the replacement string.
     *
     * If the [replacement] parameter is a function,
     * then it is invoked for each match occurrence of the pattern regex.
     * The [replacement] function must take a single parameter which will be the object structure of a regex match
     * as described in the $[match] function; and must return a string.
     *
     * The optional [limit] parameter,
     * is a number that specifies the maximum number of replacements to make before stopping.
     * The remainder of the input beyond this limit will be copied to the output unchanged.
     *
     * See [replace](https://docs.jsonata.org/string-functions#replace)
     */
    fun replace(str: JsonNode, pattern: JsonNode, replacement: JsonNode, limit: JsonNode? = null): TextNode

    /**
     * Splits the [str] parameter into an array of substrings.
     *
     * If [str] is not specified, then the context value is used as the value of str.
     *
     * It is an error if [str] is not a string.
     *
     * The [separator] parameter can either be a string or a regular expression (regex).
     * * If it is a string, it specifies the characters within str about which it should be split.
     * * If it is the empty string, [str] will be split into an array of single characters.
     * * If it is a regex, it splits the string around any sequence of characters that match the regex.
     *
     * The optional [limit] parameter is a number that specifies the maximum number of substrings
     * to include in the resultant array.
     * Any additional substrings are discarded. If limit is not specified,
     * then [str] is fully split with no limit to the size of the resultant array.
     *
     * It is an error if [limit] is not a non-negative number.
     *
     * See [split](https://docs.jsonata.org/string-functions#split)
     */
    fun split(str: JsonNode, separator: JsonNode, limit: JsonNode? = null): ArrayNode

    /**
     * Casts the [arg] parameter to a string using the following casting rules
     * * Strings are unchanged;
     * * Functions are converted to an empty string;
     * * Numeric infinity and NaN throw an error because they cannot be represented as a JSON number;
     * * All other values are converted to a JSON string using the JSON.stringify function.
     *
     * If [arg] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of [arg].
     *
     * If [prettify] is true, then "prettified" JSON is produced
     * i.e. one line per field and lines will be indented based on the field depth.
     *
     * See [string](https://docs.jsonata.org/string-functions#string)
     */
    fun string(arg: JsonNode?, prettify: BooleanNode? = null): TextNode

    /**
     * Returns a string containing the characters in the first parameter [str]
     * starting at position [start] (zero-offset).
     *
     * If [str] is not specified (i.e. this function is invoked with only the numeric argument(s)),
     * then the context value is used as the value of [str].
     *
     * An error is thrown if [str] is not a string.
     *
     * If [length] is specified, then the substring will contain maximum length characters.
     *
     * If [start] is negative then it indicates the number of characters from the end of str.
     * See [substr](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/substr)
     * for full definition.
     *
     * See [substring](https://docs.jsonata.org/string-functions#substring)
     */
    fun substring(str: JsonNode, start: DecimalNode, length: DecimalNode? = null): TextNode

    /**
     * Returns the substring after the first occurrence of the character sequence chars in [str].
     *
     * If [str] is not specified (i.e. this function is invoked with only one argument),
     * then the context value is used as the value of [str].
     *
     * If [str] does not contain chars, then it returns [str].
     *
     * An error is thrown if [str] and [chars] are not strings.
     *
     * See [substringAfter](https://docs.jsonata.org/string-functions#substringafter)
     */
    fun substringAfter(str: JsonNode, chars: TextNode): TextNode

    /**
     * Returns the substring before the first occurrence of the character sequence [chars] in [str].
     *
     * If [str] is not specified (i.e. this function is invoked with only one argument),
     * then the context value is used as the value of str.
     *
     * If [str] does not contain chars, then it returns [str].
     *
     * An error is thrown if [str] and chars are not strings.
     *
     * See [substringBefore](https://docs.jsonata.org/string-functions#substringbefore)
     */
    fun substringBefore(str: JsonNode, chars: TextNode): TextNode

    /**
     * Normalizes and trims all whitespace characters in [str] by applying the following steps:
     * * All tabs, carriage returns, and line feeds are replaced with spaces.
     * * Contiguous sequences of spaces are reduced to a single space.
     * * Trailing and leading spaces are removed.
     *
     * If [str] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of [str].
     *
     * An error is thrown if [str] is not a string.
     *
     * See [trim](https://docs.jsonata.org/string-functions#trim)
     */
    fun trim(str: JsonNode): TextNode

    /**
     * Returns a string with all the characters of [str] converted to uppercase.
     *
     * If [str] is not specified (i.e. this function is invoked with no arguments),
     * then the context value is used as the value of [str].
     *
     * An error is thrown if [str] is not a string.
     *
     * See [uppercase](https://docs.jsonata.org/string-functions#uppercase)
     */
    fun uppercase(str: JsonNode): TextNode

} //~ JSonataFunctions

