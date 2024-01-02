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
package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BooleanNode
import io.github.lucanicoladebiasi.jsong3.JSong
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/numeric-functions#parseinteger
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestBooleanFunctions {

    private val om = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = om.readTree(Thread.currentThread().contextClassLoader.getResource("address.json"))
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - boolean false`() {
        val expression = "\$boolean(false)"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - boolean true`() {
        val expression = "\$boolean(true)"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string empty`() {
        val expression = "\$boolean(\"\")"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string non-empty`() {
        val expression = "\$boolean(\"Hello world!\")"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number zero`() {
        val expression = "\$boolean(0)"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number non-zero`() {
        val expression = "\$boolean(1)"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - null`() {
        val expression = "\$boolean(null)"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array empty`() {
        val expression = "\$boolean([ ])"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array some true`() {
        val expression = "\$boolean([0, 1, 0])"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array all false`() {
        val expression = "\$boolean([0, 0, 0])"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object empty`() {
        val expression = "\$boolean([{}])"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object not empty`() {
        val expression = "\$boolean([{\"flag\": false}])"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    @Disabled
    fun `$boolean() - function`() {
        val expression = "(\$volume := function(\$l, \$w, \$h){ \$l * \$w * \$h }; \$boolean(\$volume))"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @Test
    fun `$not() - false`() {
        val expression = "\$not(false)"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @Test
    fun `$not() - true`() {
        val expression = "\$not(true)"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - false`() {
        val expression = "\$exists(Other.Nothing)"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - true`() {
        val expression = "\$exists(Address.City)"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

}