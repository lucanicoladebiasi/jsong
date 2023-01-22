package org.jsong

import com.fasterxml.jackson.databind.node.TextNode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * https://docs.jsonata.org/boolean-operators
 */
class _TestBooleanOperators {

    /**
     * https://docs.jsonata.org/boolean-operators#and-boolean-and
     */
    @Test
    fun And() {
        val expected = TextNode("Compilers: Principles, Techniques, and Tools")
        val actual = _JSong.of("library.books[\"Aho\" in authors and price < 50].title").evaluate(_TestResources.library)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-operators#or-boolean-or
     */
    @Test
    fun Or() {
        val actual = _JSong.of("library.books[price < 10 or section=\"diy\"].title").evaluate(_TestResources.library)
        assertNull(actual)
    }
}