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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.DecimalNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal


/**
 * https://docs.jsonata.org/numeric-operators
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestNumericOperators {

    private val om = ObjectMapper()

    /**
     * https://docs.jsonata.org/numeric-operators#-addition
     */
    @Test
    fun `Math - addition`() {
        val expression = "5 + 2"
        val expected = DecimalNode(BigDecimal.valueOf(7L))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#--substractionnegation
     */
    @Test
    fun `Math - subtraction`() {
        val expression = "5 - 2"
        val expected = DecimalNode(BigDecimal(3L))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#--substractionnegation
     */
    @Test
    fun `Expression - negative number`() {
        val expression = "-42"
        val expected = DecimalNode(BigDecimal(-42))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-multiplication
     */
    @Test
    fun `Math - multiplication`() {
        val expression = "5 * 2"
        val expected = DecimalNode(BigDecimal(10L))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-division
     */
    @Test
    fun `Math - division`() {
        val expression = "5 / 2"
        val expected = DecimalNode(BigDecimal(2.5))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-modulo
     */
    @Test
    fun `Math - reminder (module)`() {
        val expression = "5 % 2"
        val expected = DecimalNode(BigDecimal.ONE)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Expression - range`() {
        val expression = "[1..5]"
        val expected = listOf(1, 2, 3, 4, 5)
        val actual = JSong(expression).evaluate()?.map { it.intValue() }
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Expression - ranges with gap`() {
        val expression = "[1..3, 7..9]"
        val expected = listOf(1, 2, 3, 7, 8, 9)
        val actual = JSong(expression).evaluate()?.map { it.intValue() }
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Expression - range from function call `() {
        val expression = "[1..\$count(Items)].(\"Item \" & \$)"

        @Language("JSON")
        val expected = om.readTree(
            """
            [
              "Item 1", 
              "Item 2", 
              "Item 3"
            ]         
            """.trimIndent()
        )

        @Language("JSON")
        val node = om.readTree(
            """
            {
              "Items": [
                { "Number":  1},
                { "Number":  2},
                { "Number":  3}
              ]   
            }
        """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Range context`() {
        val expression = "[1..5].(\$ * \$)"

        @Language("JSON")
        val expected = JSong(
            """
            [1, 4, 9, 16, 25]
            """.trimIndent()
        ).evaluate()
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}