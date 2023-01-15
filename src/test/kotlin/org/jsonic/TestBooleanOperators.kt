package org.jsonic

import com.fasterxml.jackson.databind.node.TextNode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * https://docs.jsonata.org/boolean-operators
 */
class TestBooleanOperators {

    /**
     * https://docs.jsonata.org/boolean-operators#and-boolean-and
     */
    @Test
    fun And() {
        val expected = TextNode("Compilers: Principles, Techniques, and Tools")
        val actual = Processor(TestResources.library).evaluate("library.books[\"Aho\" in authors and price < 50].title")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-operators#or-boolean-or
     */
    @Test
    fun Or() {
        val actual = Processor(TestResources.library).evaluate("library.books[price < 10 or section=\"diy\"].title")
        assertNull(actual)
    }
}