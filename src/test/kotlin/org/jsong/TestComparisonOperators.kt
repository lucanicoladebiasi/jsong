package org.jsong

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
        assertTrue(JSong.of("1+1 = 2").evaluate()?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-equals
     */
    @Test
    fun `= (Equals) - strings`() {
        assertFalse(JSong.of("\"Hello\" = \"World\"").evaluate()?.booleanValue() ?: true)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-not-equals
     */
    @Test
    fun `!= (Not Equals) - numbers`() {
        assertTrue(JSong.of("1+1 != 3").evaluate()?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-not-equals
     */
    @Test
    fun `!= (Not equals) - strings`() {
        assertTrue(JSong.of("\"Hello\" != \"World\"").evaluate()?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-greater-than
     */
    @Test
    fun `Greater then - numbers`() {
        assertTrue(JSong.of("22 / 7 > 3").evaluate()?.booleanValue() ?: false)
        assertFalse(JSong.of("5 > 5").evaluate()?.booleanValue() ?: true)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-less-than
     */
    @Test
    fun `Less then - numbers`() {
        assertFalse(JSong.of("22 / 7 < 3").evaluate()?.booleanValue() ?: true)
        assertFalse(JSong.of("5 < 5").evaluate()?.booleanValue() ?: true)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-greater-than-or-equals
     */
    @Test
    fun `Greater than or equals - numbers`() {
        assertTrue(JSong.of("22 / 7 >= 3").evaluate()?.booleanValue() ?: false)
        assertTrue(JSong.of("5 >= 5").evaluate()?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#-less-than-or-equals
     */
    @Test
    fun `Less then or equals - numbers`() {
        assertFalse(JSong.of("22 / 7 <= 3").evaluate()?.booleanValue() ?: true)
        assertTrue(JSong.of("5 <= 5").evaluate()?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#in-inclusion
     */
    @Test
    fun `in (Inclusion) - in array`() {
        assertTrue(JSong.of("\"world\" in [\"hello\", \"world\"]").evaluate()?.booleanValue() ?: false)
    }

    /**
     * https://docs.jsonata.org/comparison-operators#in-inclusion
     */
    @Test
    fun `in (Inclusion) - in singleton`() {
        assertTrue(JSong.of("\"hello\" in \"hello\"").evaluate()?.booleanValue() ?: false)

    }

    /**
     * https://docs.jsonata.org/comparison-operators#in-inclusion
     */
    @Test
    fun `in (Inclusion) - in predicate`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [
              "The AWK Programming Language",
              "Compilers: Principles, Techniques, and Tools"
            ]
        """.trimIndent()
        )
        val actual = JSong.of("library.books[\"Aho\" in authors].title").evaluate(TestResources.library)
        assertEquals(expected, actual)
    }

}