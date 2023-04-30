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
package io.github.lucanicoladebiasi.jsong1

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/aggregation-functions
 */
class TestNumericAggregationFunctions {

    /**
     * https://docs.jsonata.org/aggregation-functions#sum
     */
    @Test
    fun `$sum()`() {
        val expected = Processor().evaluate("20")
        val actual = Processor().evaluate("\$sum([5,1,3,7,4])")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/aggregation-functions#max
     */
    @Test
    fun `$max()`() {
        val expected = Processor().evaluate("7")
        val actual = Processor().evaluate("\$max([5,1,3,7,4])")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/aggregation-functions#min
     */
    @Test
    fun `$min()`() {
        val expected = Processor().evaluate("1")
        val actual = Processor().evaluate("\$min([5,1,3,7,4])")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/aggregation-functions#average
     */
    @Test
    fun `$average()`() {
        val expected = Processor().evaluate("4")
        val actual = Processor().evaluate("\$average([5,1,3,7,4])")
        assertEquals(expected, actual)
    }

}