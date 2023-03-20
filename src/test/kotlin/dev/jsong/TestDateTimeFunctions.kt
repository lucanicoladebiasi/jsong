/**
 * MIT License
 *
 * Copyright (c) [2023] [Luca Nicola Debiasi]
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
package dev.jsong

import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.TextNode
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

/**
 * https://docs.JSong.org/date-time-functions
 */
class TestDateTimeFunctions {

    /**
     * https://docs.JSong.org/date-time-functions#now
     */
    @Test
    fun `$now()`() {
        val expression = "\$now()"
        val processor = Processor()
        val dtf = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())
        val expected = dtf.format(processor.time)
        assertEquals(expected, processor.evaluate(expression)!!.textValue())
    }

    /**
     * https://docs.JSong.org/date-time-functions#millis
     */
    @Test
    fun `$millis()`() {
        val expression = "\$millis()"
        val processor = Processor()
        val expected = processor.time.toEpochMilli()
        assertEquals(expected, processor.evaluate(expression)!!.asLong())
    }


    /**
     * https://docs.JSong.org/date-time-functions#frommillis
     */
    @Test
    fun `$fromMillis()`() {
        val expression = "\$fromMillis(1510067557121)"
        val processor = Processor()
        val expected = TextNode("2017-11-07T15:12:37.121Z")
        assertEquals(expected, processor.evaluate(expression))
    }

    /**
     * https://docs.JSong.org/date-time-functions#frommillis
     */
    @Test
    @Disabled("todo: JS picture format")
    fun `$fromMillis() - picture`() {
        val expression = "\$fromMillis(1510067557121, '[M01]/[D01]/[Y0001] [h#1]:[m01][P]')"
        val processor = Processor()
        val expected = TextNode("11/07/2017 3:12pm")
        assertEquals(expected, processor.evaluate(expression))
    }

    /**
     * https://docs.JSong.org/date-time-functions#frommillis
     */
    @Test
    @Disabled("todo: JS picture format")
    fun `$fromMillis() - picture and zone`() {
        val expression = "\$fromMillis(1510067557121, '[H01]:[m01]:[s01] [z]', '-0500')"
        val processor = Processor()
        val expected = TextNode("10:12:37 GMT-05:00")
        assertEquals(expected, processor.evaluate(expression))
    }

    /**
     * https://docs.JSong.org/date-time-functions#tomillis
     */
    @Test
    fun `$toMillis()`() {
        val expression = "\$toMillis(\"2017-11-07T15:07:54.972Z\")"
        val processor = Processor()
        val expected = DecimalNode(BigDecimal("1510067274972"))
        assertEquals(expected, processor.evaluate(expression))
    }
}