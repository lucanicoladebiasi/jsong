package org.jsong

import com.fasterxml.jackson.databind.node.TextNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class _TestOtherOperators {

    /**
     * https://docs.jsonata.org/other-operators#-concatenation
     */
    @Test
    fun `& (Concatenation)`() {
        val expected = TextNode("HelloWorld")
        val actual = _JSong.of("\"Hello\" & \"World\"").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/other-operators#--conditional
     */
    @Test
    fun  `Conditional - positive`() {
        val expression = "45 < 50 ? \"Cheap\" : \"Expensive\""
        val expected = TextNode("Cheap")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/other-operators#--conditional
     */
    @Test
    fun `Conditional - negative`() {
        val expression = "55 < 50 ? \"Cheap\" : \"Expensive\""
        val expected = TextNode("Expensive")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

}