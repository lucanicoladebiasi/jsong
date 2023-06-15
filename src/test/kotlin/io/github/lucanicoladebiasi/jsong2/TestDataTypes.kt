/**
 * MIT License
 *
 * Copyright (c) 2023 Luca Nicola Debiasi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDataTypes {

    private val mapr = ObjectMapper()

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - array`() {
        @Language("JSON")
        val expression =
            """
            [
              "value1",
              "value2"
            ]
            """
        val expected = mapr.readTree(expression)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - false`() {
        val expression = "false"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - null`() {
        val expression = "null"
        val expected = NullNode.instance
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - number`() {
        val expression = "-12.35e+7"
        val expected = DecimalNode("-12.35e+7".toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - object`() {
        @Language("JSON")
        val expression = """
        {
            "key1": "value1", 
            "key2": "value2"
        }
        """.trimIndent()
        val expected = mapr.readTree(expression)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

//    @Test
//    fun `Literal - range twisted`() {
//        val expression = "[3.14159265359..2.718281828459]"
//        val expected = RangesNode().add(RangeNode.of("2.718281828459".toBigDecimal(), "3.14159265359".toBigDecimal()))
//        val actual = Processor().evaluate(expression)
//        assertEquals(expected, actual)
//    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
//    @Test
//    fun `Literal - range as array`() {
//        val expression = "[1..3]"
//        val expected = TestResources1.mapper.createArrayNode().add(1).add(2).add(3)
//        val actual = Processor().evaluate(expression)
//        assertTrue(actual is RangesNode)
//        assertEquals(expected, actual.indexes)
//    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
//    @Test
//    fun `Literal - range with gap`() {
//        val expression = "[1..3, 5..7]"
//        val expected = TestResources1.mapper.createArrayNode().add(1).add(2).add(3).add(5).add(6).add(7)
//        val actual = Processor().evaluate(expression)
//        assertTrue(actual is RangesNode)
//        assertEquals(expected, actual.indexes)
//    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
//    @Test
//    fun `Literal - range with overlapping gap`() {
//        val expression = "[1..3, 5..7, 2..4]"
//        val expected = TestResources1.mapper.createArrayNode().add(1).add(2).add(3).add(4).add(5).add(6).add(7)
//        val actual = Processor().evaluate(expression)
//        assertTrue(actual is RangesNode)
//        assertEquals(expected, actual.indexes)
//    }


    /**
     * https://docs.jsonata.org/regex
     */
//    @Test
//    fun REGEX() {
//        val expression = "/[a-z]*an[a-z]*/i"
//        val expected = RegexNode(expression)
//        val actual = Processor().evaluate(expression)
//        assertEquals(expected, actual)
//    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - text`() {
        @Language("JSON")
        val expression = """
            "God's in his heaven — All's right with the world!"
        """.trimIndent()
        val expected = mapr.readTree(expression)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - true`() {
        val expression = "true"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}