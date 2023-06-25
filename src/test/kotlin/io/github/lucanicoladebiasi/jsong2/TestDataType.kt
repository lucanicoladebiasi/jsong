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
class TestDataType {

    private val mapper = ObjectMapper()

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
        val expected = mapper.readTree(expression)
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