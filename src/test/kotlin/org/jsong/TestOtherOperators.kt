package org.jsong

import com.fasterxml.jackson.databind.node.TextNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestOtherOperators {

    /**
     * https://docs.jsonata.org/other-operators#-concatenation
     */
    @Test
    fun `& (Concatenation)`() {
        val expected = TextNode("HelloWorld")
        val actual = JSong.of("\"Hello\" & \"World\"").evaluate()
        assertEquals(expected, actual)
    }

}