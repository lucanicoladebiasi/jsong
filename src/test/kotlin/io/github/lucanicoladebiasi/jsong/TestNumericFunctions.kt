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
package io.github.lucanicoladebiasi.jsong

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertTrue

/**
 * https://docs.jsonata.org/numeric-functions
 */
class TestNumericFunctions {

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Test
    fun `$number`() {
        assertEquals(
            Processor().evaluate("5"),
            Processor().evaluate("\$number(\"5\")")
        )
//        assertEquals(
//            Processor().evaluate("[1, 2, 3, 4, 5]").,
//            Processor().evaluate("[\"1\", \"2\", \"3\", \"4\", \"5\"].\$number()").
//        )
    }

    /**
     * https://docs.jsonata.org/numeric-functions#abs
     */
    @Test
    fun `$abs`() {
        assertEquals(
            Processor().evaluate("5"),
            Processor().evaluate("\$abs(5)"))
        assertEquals(
            Processor().evaluate("5"),
            Processor().evaluate("\$abs(-5)"))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    @Test
    fun `$floor()`() {
        assertEquals(Processor().evaluate("5"), Processor().evaluate("\$floor(5)"))
        assertEquals(Processor().evaluate("5"), Processor().evaluate("\$floor(5.3)"))
        assertEquals(Processor().evaluate("5"), Processor().evaluate("\$floor(5.8)"))
        assertEquals(Processor().evaluate("-6"), Processor().evaluate("\$floor(-5.3)"))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#ceil
     */
    @Test
    fun `$ceil()`() {
        assertEquals(Processor().evaluate("5"), Processor().evaluate("\$ceil(5)"))
        assertEquals(Processor().evaluate("6"), Processor().evaluate("\$ceil(5.3)"))
        assertEquals(Processor().evaluate("6"), Processor().evaluate("\$ceil(5.8)"))
        assertEquals(Processor().evaluate("-5"), Processor().evaluate("\$ceil(-5.3)"))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @Test
    fun `$round()`() {
        assertEquals(Processor().evaluate("123"), Processor().evaluate("\$round(123.456)"))
        assertEquals(Processor().evaluate("123.46"), Processor().evaluate("\$round(123.456, 2)"))
        assertEquals(Processor().evaluate("120"), Processor().evaluate("\$round(123.456, -1)"))
        assertEquals(Processor().evaluate("100"), Processor().evaluate("\$round(123.456, -2)"))
        assertEquals(Processor().evaluate("12"), Processor().evaluate("\$round(11.5)"))
        assertEquals(Processor().evaluate("12"), Processor().evaluate("\$round(12.5)"))
        assertEquals(Processor().evaluate("120"), Processor().evaluate("\$round(125, -1)"))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    @Test
    fun `$power()`() {
        assertEquals(Processor().evaluate("256"), Processor().evaluate("\$power(2, 8)"))
        //assertEquals(Processor().evaluate("1.414213562373"), Processor().evaluate("\$power(2, 0.5)")..json)
        //assertEquals(Processor().evaluate("0.25"), Processor().evaluate("\$power(2, -2)")..json)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    @Test
    fun `$sqrt()`() {
        assertEquals(Processor().evaluate("2"), Processor().evaluate("\$sqrt(4)"))
        assertEquals(Processor().evaluate("1.4142135623730951"), Processor().evaluate("\$sqrt(2)"))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#random
     */
    @Test
    fun `$random()`() {
        val expected = Processor().evaluate("\$random()")!!.decimalValue()
        assertTrue(BigDecimal.ZERO <= expected)
        assertTrue(expected < BigDecimal.ONE)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @Test
    @Disabled("todo: special formats")
    fun `$formatNumber()`() {
        assertEquals(
            Processor().evaluate("\"12,345.60\""),
            Processor().evaluate("\$formatNumber(12345.6, '#,###.00')"))
        assertEquals(
            Processor().evaluate("\"12.346e2\""),
            Processor().evaluate("\$formatNumber(1234.5678, \"00.000e0\")")
        )
        assertEquals(Processor().evaluate("\"34.56\""), Processor().evaluate("\$formatNumber(34.555, \"#0.00;(#0.00)\")"))
        assertEquals(
            Processor().evaluate("\"(34.56)\""),
            Processor().evaluate("\$formatNumber(-34.555, \"#0.00;(#0.00)\")")
        )
        assertEquals(
            Processor().evaluate("\"14%\""),
            Processor().evaluate("\$formatNumber(0.14, \"01%\")"))
        assertEquals(
            Processor().evaluate("\"140pm\""),
            Processor().evaluate("\$formatNumber(0.14, \"###pm\", {\"per-mille\": \"pm\"})")
        )
        assertEquals(
            Processor().evaluate("\"①②.③④⑥e②\""),
            Processor().evaluate(" \$formatNumber(1234.5678, \"①①.①①①e①\", {\"zero-digit\": \"\\u245f\"})")
        )
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatbase
     */
    @Test
    fun `$formatBase()`() {
        assertEquals(
            Processor().evaluate("\"1100100\""),
            Processor().evaluate("\$formatBase(100, 2)"))
        assertEquals(
            Processor().evaluate("\"9fb\""),
            Processor().evaluate("\$formatBase(2555, 16)"))
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatinteger
     */
    @Test
    @Disabled("todo: parse numeric/roman words")
    fun `$formatInteger()`() {
        assertEquals(
            Processor().evaluate("\"two thousand, seven hundred and eighty-nine\""),
            Processor().evaluate("\$formatInteger(2789, 'w')")
        )
        assertEquals(
            Processor().evaluate("\"MCMXCIX\""),
            Processor().evaluate("\$formatInteger(1999, 'I')")
        )
    }

    /**
     * https://docs.jsonata.org/numeric-functions#parseinteger
     */
    @Test
    @Disabled("todo: parse numeric/roman words")
    fun `$parseInteger()`() {
        assertEquals(
            Processor().evaluate("12476"),
            Processor().evaluate("\$parseInteger(\"twelve thousand, four hundred and seventy-six\", 'w')")
        )
        assertEquals(
            Processor().evaluate("12345678"),
            Processor().evaluate("\$parseInteger('12,345,678', '#,##0')")
        )
    }

}