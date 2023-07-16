package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong2.JSong
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StringFunctionsTest {

    @Test
    fun `$string`() {
    }

    /**
     * https://docs.jsonata.org/string-functions#length
     */
    @Test
    fun `$length`() {
        val expression = "\$length(\"Hello World\")"
        val actual = JSong(expression).evaluate()
        val expected = IntNode(11)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring - positive start`() {
        val expression = "\$substring(\"Hello World\", 3)"
        val actual = JSong(expression).evaluate()
        val expected = TextNode("lo World")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - positive start and length`() {
        val expression = "\$substring(\"Hello World\", 3, 5)"
        val actual = JSong(expression).evaluate()
        val expected = TextNode("lo Wo")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - negative start`() {
        val expression = "\$substring(\"Hello World\", -4)"
        val actual = JSong(expression).evaluate()
        val expected = TextNode("orld")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - negative start and length`() {
        val expression = "\$substring(\"Hello World\", -4, 2)"
        val actual = JSong(expression).evaluate()
        val expected = TextNode("or")
        assertEquals(expected, actual)
    }

    @Test
    fun `$substringBefore`() {
    }

    @Test
    fun `$substringAfter`() {
    }

    @Test
    fun `$uppercase`() {
    }

    @Test
    fun `$lowercase`() {
    }

    @Test
    fun `$trim`() {
    }

    @Test
    fun `$pad`() {
    }

    @Test
    fun `$contains`() {
    }

    @Test
    fun `test$contains`() {
    }

    @Test
    fun `$split`() {
    }

    @Test
    fun `$join`() {
    }

    @Test
    fun `$match`() {
    }

    @Test
    fun `$replace`() {
    }

    @Test
    fun `$eval`() {
    }

    @Test
    fun `$$base64encode`() {
    }

    @Test
    fun `$$base64decode`() {
    }

    @Test
    fun `$encodeUrlComponent`() {
    }

    @Test
    fun `$encodeUrl`() {
    }

    @Test
    fun `$decodeUrlComponent`() {
    }

    @Test
    fun `$$decodeUrl`() {
    }
}