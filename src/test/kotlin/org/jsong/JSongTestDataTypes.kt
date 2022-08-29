package org.jsong

import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JSonataTestDataTypes {

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - array`() {
        val expected = TestDocs.mapper.createArrayNode().add("value1").add("value2")
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

//    @Test
//    fun visitValue_interval() {
//        val expression = "2.718281828459~3.14159265359"
//        val evaluation = JSonata.of(expression).evaluate()
//        val expected = IntervalNode(JsonNodeFactory.instance, "2.718281828459".toBigDecimal(), "3.14159265359".toBigDecimal())
//        assertEquals(expected, evaluation.context)
//    }

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
        val expected = TestDocs.mapper.createObjectNode()
            .set<ObjectNode>("key1", TextNode("value1"))
            .set<ObjectNode>("key2", TextNode("value2"))
        val actual = JSong.of("{\"key1\": \"value1\", \"key2\": \"value2\"}").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://tradexchain.atlassian.net/wiki/spaces/MP/pages/3253174275/HOWTO+match+documents+with+JSonata+and+ng-jsonata-lib
     */
//    @Test
//    fun Percent() {
//        val expression = "10 2.5%"
//        val expected = JSON.of("0.25")
//        val evaluation = JSonata.of(expression).evaluate()
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
//    @Test
//    fun Range() {
//        val expression = "[1..3]"
//        val evaluation = JSonata.of(expression).evaluate()
//        val expected = JSON.of("[1, 2, 3]")
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
//    @Test
//    fun `Range with gap`() {
//        val expression = "[1..3, 5..7]"
//        val evaluation = JSonata.of(expression).evaluate()
//        val expected = JSON.of("[1, 2, 3, 5, 6, 7]")
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
//    @Test
//    fun `Range with gaps overlapping`() {
//        val expression = "[1..3, 5..7, 2..4]"
//        val evaluation = JSonata.of(expression).evaluate()
//        val expected = JSON.of("[1, 2, 3, 4, 5, 6, 7]")
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/regex
     */
//    @Test
//    fun REGEX() {
//        val expression = "/[a-z]*an[a-z]*/i"
//        val evaluation = JSonata.of(expression).evaluate()
//        val expected = RegexNode(expression)
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun Text() {
        val text = "\"God's in his heaven — All's right with the world!\""
        val expected = TextNode(text.substring(1, text.length - 1))
        val actual = JSong.of(text).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun True() {
        val expected = BooleanNode.TRUE
        val actual = JSong.of("true").evaluate()
        assertEquals(expected, actual)
    }

}