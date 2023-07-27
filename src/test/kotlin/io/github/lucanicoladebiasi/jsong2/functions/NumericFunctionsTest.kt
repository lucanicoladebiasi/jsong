package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NumericNode
import io.github.lucanicoladebiasi.jsong2.JSong
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NumericFunctionsTest {

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
    fun `$abs`() {
    }

    @Test
    fun `$ceil`() {
    }

    @Test
    fun `$round`() {
    }

    @Test
    fun `test$round`() {
    }

    @Test
    fun `$power`() {
    }

    @Test
    fun `test$power`() {
    }

    @Test
    fun `$random`() {
    }

    @Test
    fun `$formatNumber`() {
    }

    @Test
    fun `test$formatNumber`() {
    }

    @Test
    fun `$formatBase`() {
    }

    @Test
    fun `test$formatBase`() {
    }

    @Test
    fun `$formatInteger`() {
    }

    @Test
    fun `$parseInteger`() {
    }
}