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

import com.fasterxml.jackson.databind.node.DecimalNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal


/**
 * https://docs.jsonata.org/numeric-operators
 */
class TestNumericOperators {

    /**
     * https://docs.jsonata.org/numeric-operators#-addition
     */
    @Test
    fun Addition() {
        val expression = "5 + 2"
        val expected = DecimalNode(BigDecimal.valueOf(7L))
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#--substractionnegation
     */
    @Test
    fun Subtraction() {
        val expression = "5 - 2"
        val expected = DecimalNode(BigDecimal(3L))
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#--substractionnegation
     */
    @Test
    fun Negation() {
        val expression = "-42"
        val expected = DecimalNode(BigDecimal(-42))
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-multiplication
     */
    @Test
    fun Multiplication() {
        val expression = "5 * 2"
        val expected = DecimalNode(BigDecimal(10L))
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-division
     */
    @Test
    fun Division() {
        val expression = "5 / 2"
        val expected = DecimalNode(BigDecimal(2.5))
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-modulo
     */
    @Test
    fun Reminder() {
        val expression = "5 % 2"
        val expected = DecimalNode(BigDecimal.ONE)
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun Range() {
        val expression = "[1..5]"
        val expected = TestResources.mapper.readTree("[1, 2, 3, 4, 5]")
        val actual = (Processor().evaluate(expression) as RangesNode).indexes
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Range gap`() {
        val expression = "[1..3, 7..9]"
        val expected = TestResources.mapper.readTree("[1, 2, 3, 7, 8, 9]")
        val actual = (Processor().evaluate(expression) as RangesNode).indexes
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Range expression`() {
        val expression = "[1..\$count(Items)].(\"Item \" & \$)"
        val expected = TestResources.mapper.readTree("[\"Item 1\",\"Item 2\",\"Item 3\"]")
        val actual = Processor(TestResources.items).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Range context`() {
        val expression = "[1..5].(\$ * \$)"
        val expected = TestResources.mapper.createArrayNode()
            .add(BigDecimal(1L))
            .add(BigDecimal(4L))
            .add(BigDecimal(9L))
            .add(BigDecimal(16L))
            .add(BigDecimal(25L))
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

}