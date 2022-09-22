package org.jsong

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertTrue

/**
 * https://docs.jsonata.org/numeric-functions
 */
class TestNumericFunctions {

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Test
    fun `$number`() {
        assertEquals(JSong.of("5").evaluate(), JSong.of("\$number(\"5\")").evaluate())
//        assertEquals(
//            JSong.of("[1, 2, 3, 4, 5]").evaluate(),
//            JSong.of("[\"1\", \"2\", \"3\", \"4\", \"5\"].\$number()").evaluate()
//        )
    }

    /**
     * https://docs.jsonata.org/numeric-functions#abs
     */
    @Test
    fun `$abs`() {
        assertEquals(JSong.of("5").evaluate(), JSong.of("\$abs(5)").evaluate())
        assertEquals(JSong.of("5").evaluate(), JSong.of("\$abs(-5)").evaluate())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    @Test
    fun `$floor()`() {
        assertEquals(JSong.of("5").evaluate(), JSong.of("\$floor(5)").evaluate())
        assertEquals(JSong.of("5").evaluate(), JSong.of("\$floor(5.3)").evaluate())
        assertEquals(JSong.of("5").evaluate(), JSong.of("\$floor(5.8)").evaluate())
        assertEquals(JSong.of("-6").evaluate(), JSong.of("\$floor(-5.3)").evaluate())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#ceil
     */
    @Test
    fun `$ceil()`() {
        assertEquals(JSong.of("5").evaluate(), JSong.of("\$ceil(5)").evaluate())
        assertEquals(JSong.of("6").evaluate(), JSong.of("\$ceil(5.3)").evaluate())
        assertEquals(JSong.of("6").evaluate(), JSong.of("\$ceil(5.8)").evaluate())
        assertEquals(JSong.of("-5").evaluate(), JSong.of("\$ceil(-5.3)").evaluate())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @Test
    fun `$round()`() {
        assertEquals(JSong.of("123").evaluate(), JSong.of("\$round(123.456)").evaluate())
        assertEquals(JSong.of("123.46").evaluate(), JSong.of("\$round(123.456, 2)").evaluate())
        assertEquals(JSong.of("120").evaluate(), JSong.of("\$round(123.456, -1)").evaluate())
        assertEquals(JSong.of("100").evaluate(), JSong.of("\$round(123.456, -2)").evaluate())
        assertEquals(JSong.of("12").evaluate(), JSong.of("\$round(11.5)").evaluate())
        assertEquals(JSong.of("12").evaluate(), JSong.of("\$round(12.5)").evaluate())
        assertEquals(JSong.of("120").evaluate(), JSong.of("\$round(125, -1)").evaluate())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    @Test
    fun `$power()`() {
        assertEquals(JSong.of("256").evaluate(), JSong.of("\$power(2, 8)").evaluate())
        //assertEquals(JSong.of("1.414213562373"), JSong.of("\$power(2, 0.5)").evaluate().json)
        //assertEquals(JSong.of("0.25"), JSong.of("\$power(2, -2)").evaluate().json)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    @Test
    fun `$sqrt()`() {
        assertEquals(JSong.of("2").evaluate(), JSong.of("\$sqrt(4)").evaluate())
        assertEquals(JSong.of("1.4142135623730951").evaluate(), JSong.of("\$sqrt(2)").evaluate())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#random
     */
    @Test
    fun `$random()`() {
        val expected = JSong.of("\$random()").evaluate()!!.decimalValue()
        assertTrue(BigDecimal.ZERO <= expected)
        assertTrue(expected < BigDecimal.ONE)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @Test
    @Disabled
    fun `$formatNumber()`() {
        assertEquals(JSong.of("\"12,345.60\"").evaluate(), JSong.of("\$formatNumber(12345.6, '#,###.00')").evaluate())
        assertEquals(
            JSong.of("\"12.346e2\"").evaluate(),
            JSong.of("\$formatNumber(1234.5678, \"00.000e0\")").evaluate()
        )
        assertEquals(JSong.of("\"34.56\"").evaluate(), JSong.of("\$formatNumber(34.555, \"#0.00;(#0.00)\")").evaluate())
        assertEquals(
            JSong.of("\"(34.56)\"").evaluate(),
            JSong.of("\$formatNumber(-34.555, \"#0.00;(#0.00)\")").evaluate()
        )
        assertEquals(JSong.of("\"14%\"").evaluate(), JSong.of("\$formatNumber(0.14, \"01%\")").evaluate())
        assertEquals(
            JSong.of("\"140pm\"").evaluate(),
            JSong.of("\$formatNumber(0.14, \"###pm\", {\"per-mille\": \"pm\"})").evaluate()
        )
        assertEquals(
            JSong.of("\"①②.③④⑥e②\"").evaluate(),
            JSong.of(" \$formatNumber(1234.5678, \"①①.①①①e①\", {\"zero-digit\": \"\\u245f\"})").evaluate()
        )
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatbase
     */
    @Test
    fun `$formatBase()`() {
        assertEquals(JSong.of("\"1100100\"").evaluate(), JSong.of("\$formatBase(100, 2)").evaluate())
        assertEquals(JSong.of("\"9fb\"").evaluate(), JSong.of("\$formatBase(2555, 16)").evaluate())
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatinteger
     */
    @Test
    @Disabled
    fun `$formatInteger()`() {
        assertEquals(
            JSong.of("\"two thousand, seven hundred and eighty-nine\"").evaluate(),
            JSong.of("\$formatInteger(2789, 'w')").evaluate()
        )
        assertEquals(
            JSong.of("\"MCMXCIX\"").evaluate(),
            JSong.of("\$formatInteger(1999, 'I')").evaluate()
        )
    }

    /**
     * https://docs.jsonata.org/numeric-functions#parseinteger
     */
    @Test
    @Disabled
    fun `$parseInteger()`() {
        assertEquals(
            JSong.of("12476").evaluate(),
            JSong.of("\$parseInteger(\"twelve thousand, four hundred and seventy-six\", 'w')").evaluate()
        )
        assertEquals(
            JSong.of("12345678").evaluate(),
            JSong.of("\$parseInteger('12,345,678', '#,##0')").evaluate()
        )
    }

}