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
package io.github.lucanicoladebiasi.jsong1

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/comparison-operators
 */
class TestComparisonOperators {

    /**
     * https://docs.jsonata.org/comparison-operators#-equals
     */
    @Test
    fun `= (Equals) - numbers`() {
        val expression = "1+1 = 2"
        assertTrue(Processor().evaluate(expression)?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-equals
     */
    @Test
    fun `= (Equals) - strings`() {
        val expression = "\"Hello\" = \"World\""
        assertFalse(Processor().evaluate(expression)?.booleanValue() ?: true)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-not-equals
     */
    @Test
    fun `!= (Not Equals) - numbers`() {
        val expression = "1+1 != 3"
        assertTrue(Processor().evaluate(expression)?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-not-equals
     */
    @Test
    fun `!= (Not equals) - strings`() {
        val expression = "\"Hello\" != \"World\""
        assertTrue(Processor().evaluate(expression)?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-greater-than
     */
    @Test
    fun `Greater then - numbers - between different`() {
        val expression = "22 / 7 > 3"
        assertTrue(Processor().evaluate(expression)?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-greater-than
     */
    @Test
    fun `Greater then - numbers - between equal`() {
        val expression = "5 > 5"
        assertFalse(Processor().evaluate(expression)?.booleanValue() ?: true)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-less-than
     */
    @Test
    fun `Less then - numbers - between different`() {
        val expression = "22 / 7 < 3"
        assertFalse(Processor().evaluate(expression)?.booleanValue() ?: true)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-less-than
     */
    @Test
    fun `Less then - numbers -between equal`() {
        val expression = "5 < 5"
        assertFalse(Processor().evaluate(expression)?.booleanValue() ?: true)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-greater-than-or-equals
     */
    @Test
    fun `Greater than or equals - numbers - between different`() {
        val expression = "22 / 7 >= 3"
        assertTrue(Processor().evaluate(expression)?.booleanValue() ?: false)
    }

    /**
    * https://docs.jsonata.org/comparison-operators#-greater-than-or-equals
    */
    @Test
    fun `Greater than or equals - numbers - between equal`() {
        val expression = "5 >= 5"
        assertTrue(Processor().evaluate(expression)?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-less-than-or-equals
     */
    @Test
    fun `Less then or equals - numbers - positive `() {
        val expression = "22 / 7 <= 3"
        assertFalse(Processor().evaluate(expression)?.booleanValue() ?: true)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-less-than-or-equals
     */
    @Test
    fun `Less then or equals - numbers - negative`() {
        val expression = "5 <= 5"
        assertTrue(Processor().evaluate(expression)?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#in-inclusion
     */
    @Test
    fun `in (Inclusion) - in array`() {
        val expression = "\"world\" in [\"hello\", \"world\"]"
        assertTrue(Processor().evaluate(expression)?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#in-inclusion
     */
    @Test
    fun `in (Inclusion) - in singleton`() {
        val expression = "\"hello\" in \"hello\""
        assertTrue(Processor().evaluate(expression)?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#in-inclusion
     */
    @Test
    fun `in (Inclusion) - in predicate`() {
        val expression = "library.books[\"Aho\" in authors].title"

        @Language("JSON")
        val expected = TestResources1.mapper.readTree(
            """
            [
              "The AWK Programming Language",
              "Compilers: Principles, Techniques, and Tools"
            ]
        """.trimIndent()
        )
        val actual = Processor(TestResources1.library).evaluate(expression)
        assertEquals(expected, actual)
    }

}