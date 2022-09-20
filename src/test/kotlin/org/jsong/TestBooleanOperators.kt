package org.jsong

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
        val actual = JSong.of("library.books[\"Aho\" in authors and price < 50].title").evaluate(TestResources.library)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-operators#or-boolean-or
     */
    @Test
    fun Or() {
        val actual = JSong.of("library.books[price < 10 or section=\"diy\"].title").evaluate(TestResources.library)
        assertNull(actual)
    }
}