package org.jsonic

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
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
    @Disabled
    fun `in (Inclusion) - in predicate`() {
        val expression = "library.books[\"Aho\" in authors].title"

        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [
              "The AWK Programming Language",
              "Compilers: Principles, Techniques, and Tools"
            ]
        """.trimIndent()
        )
        val actual = Processor(TestResources.library).evaluate(expression)
        assertEquals(expected, actual)
    }

}