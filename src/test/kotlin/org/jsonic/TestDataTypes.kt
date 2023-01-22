package org.jsonic

import com.fasterxml.jackson.databind.node.*
import org.jsong._TestResources
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TestDataTypes {

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - array`() {
        val expression = "[\"value1\", \"value2\"]"
        val expected = _TestResources.mapper.createArrayNode().add("value1").add("value2")
        val actual =  Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - false`() {
        val expression = "false"
        val expected = BooleanNode.FALSE
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - null`() {
        val expression = "null"
        val expected = NullNode.instance
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - number`() {
        val expression = "-12.35e+7"
        val expected = DecimalNode("-12.35e+7".toBigDecimal())
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - object`() {
        val expression = "{\"key1\": \"value1\", \"key2\": \"value2\"}"
        val expected = _TestResources.mapper.createObjectNode()
            .set<ObjectNode>("key1", TextNode("value1"))
            .set<ObjectNode>("key2", TextNode("value2"))
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    @Test
    fun `Literal - range twisted`() {
        val expression = "[3.14159265359..2.718281828459]"
        val expected = RangesNode().add(RangeNode.of("2.718281828459".toBigDecimal(), "3.14159265359".toBigDecimal()))
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Literal - range as array`()  {
        val expression = "[1..3]"
        val expected = _TestResources.mapper.createArrayNode().add(1).add(2).add(3)
        val actual = Processor().evaluate(expression)
        assertTrue(actual is RangesNode)
        assertEquals(expected, actual.indexes)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Literal - range with gap`() {
        val expression = "[1..3, 5..7]"
        val expected = _TestResources.mapper.createArrayNode().add(1).add(2).add(3).add(5).add(6).add(7)
        val actual = Processor().evaluate(expression)
        assertTrue(actual is RangesNode)
        assertEquals(expected, actual.indexes)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Literal - range with overlapping gap`() {
        val expression = "[1..3, 5..7, 2..4]"
        val expected = _TestResources.mapper.createArrayNode().add(1).add(2).add(3).add(4).add(5).add(6).add(7)
        val actual = Processor().evaluate(expression)
        assertTrue(actual is RangesNode)
        assertEquals(expected, actual.indexes)
    }


    /**
     * https://docs.jsonata.org/regex
     */
    @Test
    fun REGEX() {
        val expression = "/[a-z]*an[a-z]*/i"
        val expected = RegexNode(expression)
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - text`() {
        val expression = "\"God's in his heaven — All's right with the world!\""
        val expected = TextNode(expression.substring(1, expression.length - 1))
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - true`() {
        val expression = "true"
        val expected = BooleanNode.TRUE
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

}