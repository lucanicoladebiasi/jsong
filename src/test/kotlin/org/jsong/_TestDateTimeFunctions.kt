package org.jsong

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
class _TestDateTimeFunctions {

    /**
     * https://docs.JSong.org/date-time-functions#now
     */
    @Test
    fun `$now()`() {
        val expression = "\$now()"
        val processor = _JSong.of(expression)
        val dtf = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())
        val expected = dtf.format(processor.time)
        assertEquals(expected, processor.evaluate()!!.textValue())
    }

    /**
     * https://docs.JSong.org/date-time-functions#millis
     */
    @Test
    fun `$millis()`() {
        val expression = "\$millis()"
        val processor = _JSong.of(expression)
        val expected = processor.time.toEpochMilli()
        assertEquals(expected, processor.evaluate()!!.asLong())
    }


    /**
     * https://docs.JSong.org/date-time-functions#frommillis
     */
    @Test
    fun `$fromMillis()`() {
        val expression = "\$fromMillis(1510067557121)"
        val processor = _JSong.of(expression)
        val expected = TextNode("2017-11-07T15:12:37.121Z")
        assertEquals(expected, processor.evaluate())
    }

    /**
     * https://docs.JSong.org/date-time-functions#frommillis
     */
    @Test
    @Disabled("todo: JS picture format")
    fun `$fromMillis() - picture`() {
        val expression = "\$fromMillis(1510067557121, '[M01]/[D01]/[Y0001] [h#1]:[m01][P]')"
        val processor = _JSong.of(expression)
        val expected = TextNode("11/07/2017 3:12pm")
        assertEquals(expected, processor.evaluate())
    }

    /**
     * https://docs.JSong.org/date-time-functions#frommillis
     */
    @Test
    @Disabled("todo: JS picture format")
    fun `$fromMillis() - picture and zone`() {
        val expression = "\$fromMillis(1510067557121, '[H01]:[m01]:[s01] [z]', '-0500')"
        val processor = _JSong.of(expression)
        val expected = TextNode("10:12:37 GMT-05:00")
        assertEquals(expected, processor.evaluate())
    }

    /**
     * https://docs.JSong.org/date-time-functions#tomillis
     */
    @Test
    fun `$toMillis()`() {
        val expression = "\$toMillis(\"2017-11-07T15:07:54.972Z\")"
        val processor = _JSong.of(expression)
        val expected = DecimalNode(BigDecimal("1510067274972"))
        assertEquals(expected, processor.evaluate())
    }
}