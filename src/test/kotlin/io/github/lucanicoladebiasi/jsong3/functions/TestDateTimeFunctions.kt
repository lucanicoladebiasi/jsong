package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong3.JSong
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Disabled
import java.math.BigDecimal
import kotlin.test.assertEquals

class TestDateTimeFunctions {

    /**
     * https://docs.jsonata.org/date-time-functions#now
     */
    @Test
    @Disabled
    fun `$now`() {
        val expression = "\$now()"

    }

    /**
     * https://docs.jsonata.org/date-time-functions#now
     */
    @Test
    @Disabled
    fun `$now - picture`() {
        val expression = "\$now()"
        val actual = JSong(expression).evaluate()
        //assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/date-time-functions#now
     */
    @Test
    @Disabled
    fun `$now - picture and timezone`() {
        val expression = "\$now()"
        val actual = JSong(expression).evaluate()
        //assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/date-time-functions#millis
     */
    @Test
    @Disabled
    fun `$millis`() {
        val expression = "\$millis()"
        val actual = JSong(expression).evaluate()
        //assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/date-time-functions#frommillis
     */
    @Test
    @Disabled
    fun `$fromMillis`() {
        val expression = "\$fromMillis(1510067557121)"
        val expected = TextNode("2017-11-07T15:12:37.121Z")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/date-time-functions#frommillis
     */
    @Test
    @Disabled
    fun `$fromMillis - picture`() {
        val expression = "\$fromMillis(1510067557121, '[M01]/[D01]/[Y0001] [h#1]:[m01][P]')"
        val expected = TextNode("11/07/2017 3:12pm")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/date-time-functions#frommillis
     */
    @Test
    @Disabled
    fun `$fromMillis - picture and timezone`() {
        val expression = "\$fromMillis(1510067557121, '[H01]:[m01]:[s01] [z]', '-0500')"
        val expected = TextNode("10:12:37 GMT-05:00")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/date-time-functions#tomillis
     */
    @Test
    @Disabled
    fun `$toMillis`() {
        val expression = "\$toMillis(\"2017-11-07T15:07:54.972Z\")"
        val expected = DecimalNode(BigDecimal("1510067274972"))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/date-time-functions#tomillis
     */
    @Test
    @Disabled
    fun `$toMillis - picture`() {
        val expression = "\$toMillis(\"2017-11-07T15:07:54.972Z\")"
        val expected = DecimalNode(BigDecimal("1510067274972"))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}