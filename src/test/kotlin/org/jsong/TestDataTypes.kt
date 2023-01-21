package org.jsong

import com.fasterxml.jackson.databind.node.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TestDataTypes {

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - array`() {
        val expected = TestResources.mapper.createArrayNode().add("value1").add("value2")
        val actual = JSong.of("[\"value1\", \"value2\"]").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - false`() {
        val expected = BooleanNode.FALSE
        val actual = JSong.of("false").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - null`() {
        val expected = NullNode.instance
        val actual = JSong.of("null").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - number`() {
        val expected = DecimalNode("-12.35e+7".toBigDecimal())
        val actual = JSong.of("-12.35e+7").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - object`() {
        val expected = TestResources.mapper.createObjectNode()
            .set<ObjectNode>("key1", TextNode("value1"))
            .set<ObjectNode>("key2", TextNode("value2"))
        val actual = JSong.of("{\"key1\": \"value1\", \"key2\": \"value2\"}").evaluate()
        assertEquals(expected, actual)
    }

    @Test
    fun `Literal - range twisted`() {
        val expected = RangeNode.of("2.718281828459".toBigDecimal(), "3.14159265359".toBigDecimal())
        val actual = JSong.of("[3.14159265359..2.718281828459]").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Literal - range as array`()  {
        val expected = TestResources.mapper.createArrayNode().add(1).add(2).add(3)
        val actual = JSong.of("[1..3]").evaluate()
        assertTrue(actual is RangeNode)
        assertEquals(expected, actual.indexes)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Literal - range with gap`() {
        val expected = TestResources.mapper.createArrayNode().add(1).add(2).add(3).add(5).add(6).add(7)
        val actual = JSong.of("[1..3, 5..7]").evaluate()
        assertTrue(actual is _RangesNode)
        assertEquals(expected, actual.indexes)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Literal - range with overlapping gap`() {
        val expected = TestResources.mapper.createArrayNode().add(1).add(2).add(3).add(4).add(5).add(6).add(7)
        val actual = JSong.of("[1..3, 5..7, 2..4]").evaluate()
        assertTrue(actual is _RangesNode)
        assertEquals(expected, actual.indexes)
    }


    /**
     * https://docs.jsonata.org/regex
     */
    @Test
    fun REGEX() {
        val expression = "/[a-z]*an[a-z]*/i"
        val actual = JSong.of(expression).evaluate()
        val expected = _RegexNode(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - text`() {
        val text = "\"God's in his heaven — All's right with the world!\""
        val expected = TextNode(text.substring(1, text.length - 1))
        val actual = JSong.of(text).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - true`() {
        val expected = BooleanNode.TRUE
        val actual = JSong.of("true").evaluate()
        assertEquals(expected, actual)
    }

}