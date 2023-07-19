package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.regex.Pattern

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestRegexType {

    private val mapper = ObjectMapper()

    /**
     * https://docs.jsonata.org/regex
     */
    @Test
    fun `Regex - case insensitive`() {
        val expression = "/[a-z]*an[a-z]*/i"
        val expected = RegexNode(Pattern.compile(expression.substring(1, expression.length - 2), Pattern.CASE_INSENSITIVE), mapper.nodeFactory)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/regex
     */
    @Test
    fun `Regex - multiline`() {
        val expression = "/[a-z]*an[a-z]*/m"
        val expected = RegexNode(Pattern.compile(expression.substring(1, expression.length - 2), Pattern.MULTILINE), mapper.nodeFactory)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/regex
     */
    @Test
    fun `Regex - no flags`() {
        val expression = "/[a-z]*an[a-z]*/"
        val expected = RegexNode(Pattern.compile(expression.substring(1, expression.length - 1)), mapper.nodeFactory)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}