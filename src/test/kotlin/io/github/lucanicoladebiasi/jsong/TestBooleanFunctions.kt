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
package io.github.lucanicoladebiasi.jsong

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

/**
 * https://docs.jsonata.org/numeric-functions#parseinteger
 */
class TestBooleanFunctions {

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - boolean false`() {
        assertFalse(Processor().evaluate("\$boolean(false)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - boolean true`() {
        assertTrue(Processor().evaluate("\$boolean(true)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string empty`() {
        assertFalse(Processor().evaluate("\$boolean(\"\")")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string non-empty`() {
        assertTrue(Processor().evaluate("\$boolean(\"Hello world!\")")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number zero`() {
        assertFalse(Processor().evaluate("\$boolean(0)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number non-zero`() {
        assertTrue(Processor().evaluate("\$boolean(1)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - null`() {
        assertFalse(Processor().evaluate("\$boolean(null)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array empty`() {
        assertFalse(Processor().evaluate("\$boolean([ ])")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array some true`() {
        assertTrue(Processor().evaluate("\$boolean([0, 1, 0])")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array all false`() {
        assertFalse(Processor().evaluate("\$boolean([0, 0, 0])")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object empty`() {
        val actual = Processor().evaluate("\$boolean([{}])")
        assertFalse(actual!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object not empty`() {
        assertTrue(Processor().evaluate("\$boolean([{\"flag\": false}])")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - function`() {
        val expression = "(\$volume := function(\$l, \$w, \$h){ \$l * \$w * \$h }; \$boolean(\$volume))"
        assertFalse(Processor().evaluate(expression)!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @Test
    fun `$not() - false`() {
        assertTrue(Processor().evaluate("\$not(false)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @Test
    fun `$not() - true`() {
        assertFalse(Processor().evaluate("\$not(true)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - false`() {
        val actual = Processor(TestResources.address).evaluate("\$exists(Other.Nothing)")
        assertFalse(actual!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - true`() {
        val actual = Processor(TestResources.address).evaluate("\$exists(Address.City)")
        assertTrue(actual!!.booleanValue())
    }
}