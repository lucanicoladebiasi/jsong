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

import com.fasterxml.jackson.databind.node.TextNode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * https://docs.jsonata.org/boolean-operators
 */
class TestBooleanOperators {

    /**
     * https://docs.jsonata.org/boolean-operators#and-boolean-and
     */
    @Test
    fun and() {
        val expected = TextNode("Compilers: Principles, Techniques, and Tools")
        val actual = Processor(TestResources.library).evaluate("library.books[\"Aho\" in authors and price < 50].title")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/boolean-operators#or-boolean-or
     */
    @Test
    fun or() {
        val actual = Processor(TestResources.library).evaluate("library.books[price < 10 or section=\"diy\"].title")
        assertNull(actual)
    }
}