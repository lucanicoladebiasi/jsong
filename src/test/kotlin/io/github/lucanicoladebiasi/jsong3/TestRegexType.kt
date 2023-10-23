package io.github.lucanicoladebiasi.jsong3

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestRegexType {

    /**
     * https://docs.jsonata.org/regex
     */
    @Test
    fun `Regex - case insensitive`() {
        val expression = "/[a-z]*an[a-z]*/i"
        val expected = Regex(expression.substring(1, expression.length - 2), RegexOption.IGNORE_CASE)
        val actual = JSong(expression).evaluate()
        actual as RegexNode
        assertEquals(expected.pattern, actual.regex.pattern)
        assertEquals(expected.options, actual.regex.options)
    }

    /**
     * https://docs.jsonata.org/regex
     */
    @Test
    fun `Regex - multiline`() {
        val expression = "/[a-z]*an[a-z]*/m"
        val expected = Regex(expression.substring(1, expression.length - 2), RegexOption.MULTILINE)
        val actual = JSong(expression).evaluate()
        actual as RegexNode
        assertEquals(expected.pattern, actual.regex.pattern)
        assertEquals(expected.options, actual.regex.options)
    }

    /**
     * https://docs.jsonata.org/regex
     */
    @Test
    fun `Regex - no flags`() {
        val expression = "/[a-z]*an[a-z]*/"
        val expected = Regex(expression.substring(1, expression.length - 1))
        val actual = JSong(expression).evaluate()
        actual as RegexNode
        assertEquals(expected.pattern, actual.regex.pattern)
        assertEquals(expected.options, actual.regex.options)
    }

}