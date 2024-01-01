package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong3.JSong
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.text.DecimalFormat

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestNumericFunctions {

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Test
    fun `$number - boolean - false`() {
        val expression = "\$number(false)"
        val expected = DecimalNode(BigDecimal.ZERO) as NumericNode
        val actual = JSong(expression).evaluate() as NumericNode
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Test
    fun `$number - boolean - true`() {
        val expression = "\$number(true)"
        val expected = DecimalNode(BigDecimal.ONE) as NumericNode
        val actual = JSong(expression).evaluate() as NumericNode
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Test
    @Disabled
    fun `$number - context`() {
        val expression = "[\"1\", \"2\", \"3\", \"4\", \"5\"].\$number()"
        val expected = JSong("[1, 2, 3, 4, 5]").evaluate()
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Test
    fun `$number - hex`() {
        val expression = "\$number(\"0x12\")"
        val expected = DecimalNode(18.toBigDecimal()) as NumericNode
        val actual = JSong(expression).evaluate() as NumericNode
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Test
    fun `$number - numeric`() {
        val expression = "\$number(-12.35e+7)"
        val expected = DecimalNode("-12.35e+7".toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#number
     */
    @Test
    fun `$number - text`() {
        val expression = "\$number(\"5\")"
        val expected = DecimalNode(BigDecimal(5))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    @Test
    fun `$abs - negative`() {
        val expression = "\$abs(-5)"
        val expected = DecimalNode(5.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    @Test
    fun `$abs - positive`() {
        val expression = "\$abs(5)"
        val expected = DecimalNode(5.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    @Test
    fun `$floor - positive integer`() {
        val expression = "\$floor(5)"
        val expected = DecimalNode(5.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    @Test
    fun `$floor - positive lower half decimal`() {
        val expression = "\$floor(5.3)"
        val expected = DecimalNode(5.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    @Test
    fun `$floor - positive higher half decimal`() {
        val expression = "\$floor(5.8)"
        val expected = DecimalNode(5.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#floor
     */
    @Test
    fun `$floor - negative lower half decimal`() {
        val expression = "\$floor(-5.3)"
        val expected = DecimalNode(-6.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#ceil
     */
    @Test
    fun `$ceil - positive integer`() {
        val expression = "\$ceil(5)"
        val expected = DecimalNode(5.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#ceil
     */
    @Test
    fun `$ceil - positive lower half decimal`() {
        val expression = "\$ceil(5.3)"
        val expected = DecimalNode(6.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#ceil
     */
    @Test
    fun `$ceil - positive higher half decimal`() {
        val expression = "\$ceil(5.8)"
        val expected = DecimalNode(6.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#ceil
     */
    @Test
    fun `$ceil - negative lower half decimal `() {
        val expression = "\$ceil(-5.3)"
        val expected = DecimalNode(-5.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @Test
    fun `$round - no precision`() {
        val expression = "\$round(123.456)"
        val expected = DecimalNode(123.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @Test
    fun `$round - positive two digit precision`() {
        val expression = "\$round(123.456, 2)"
        val expected = DecimalNode(123.46.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @Test
    fun `$round - negative one digit precision`() {
        val expression = "\$round(123.456, -1)"
        val expected = DecimalNode(120.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @Test
    fun `$round - negative two digit precision`() {
        val expression = "\$round(123.456, -2)"
        val expected = DecimalNode(100.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @Test
    fun `$round - odd half`() {
        val expression = "\$round(11.5)"
        val expected = DecimalNode(12.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @Test
    fun `$round - even half`() {
        val expression = "\$round(12.5)"
        val expected = DecimalNode(12.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#round
     */
    @Test
    fun `$round - integer - negative one digit precision`() {
        val expression = "\$round(125, -1)"
        val expected = DecimalNode(120.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    @Test
    fun `$power - positive integer`() {
        val expression = "\$power(2, 8)"
        val expected = DecimalNode(256.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    @Test
    fun `$power- positive decimal`() {
        val expression = "\$power(2, 0.5)"
        val expected = DecimalNode("1.4142135623730951".toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#power
     */
    @Test
    fun `$power - negative integer`() {
        val expression = "\$power(2, -2)"
        val expected = DecimalNode(0.25.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    @Test
    fun `$sqrt - integer`() {
        val expression = "\$sqrt(4)"
        val expected = DecimalNode(2.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    @Test
    fun `$sqrt - not integer`() {
        val expression = "\$sqrt(2)"
        val expected = DecimalNode(BigDecimal("1.414213562373095048801688724209698"))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }


    /**
     * https://docs.jsonata.org/numeric-functions#sqrt
     */
    @Test
    fun `$random`() {
        val expression = "\$random()"
        val actual = JSong(expression).evaluate()
        assertTrue(actual?.decimalValue()?.let {  it >= BigDecimal.ZERO } ?: false)
        assertTrue(actual?.decimalValue()?.let { it < BigDecimal.ONE } ?: false)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @Test
    fun `$formatNumber - group notation`() {
        val expression = "\$formatNumber(12345.6, '#,###.00')"
        val expected = TextNode("12,345.60")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @Test
    fun `$formatNumber - e notation`() {
        val expression = "\$formatNumber(1234.5678, \"00.000E0\")"
        val expected = TextNode("12.346E2")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @Test
    @Disabled
    fun `$formatNumber - positive cents`() {
        val expression = "\$formatNumber(34.555, \"#0.00;(#0.00)\")"
        val expected = TextNode("34.55")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @Test
    @Disabled
    fun `$formatNumber - negative cents`() {
        val expression = "\$formatNumber(-34.555, \"#0.00;(#0.00)\")"
        val expected = TextNode("(34.55)")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @Test
    fun `$formatNumber - percent`() {
        val expression = "\$formatNumber(0.14, \"00%\")"
        val expected = TextNode("14%")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @Test
    fun `$formatNumber - per-mille option`() {
        val expression = "\$formatNumber(0.14, \"###pm\", {\"per-mille\": \"pm\"})"
        val expected = TextNode("140pm")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-functions#formatnumber
     */
    @Test
    @Disabled
    fun `$formatNumber - zero-digit option`() {
        val expression = "\$formatNumber(1234.5678, \"①①.①①①e①\", {\"zero-digit\": \"\\u245f\"})"
        val expected = TextNode("\"①②.③④⑥e②\"")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `$formatBase`() {
    }

    @Test
    @Disabled
    fun `test$formatBase`() {
    }

    @Test
    @Disabled
    fun `$formatInteger`() {
    }

    @Test
    @Disabled
    fun `$parseInteger`() {
    }
}