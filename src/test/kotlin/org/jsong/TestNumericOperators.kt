package org.jsong

import com.fasterxml.jackson.databind.node.DecimalNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal


/**
 * https://docs.jsonata.org/numeric-operators
 */
class TestNumericOperators {

    /**
     * https://docs.jsonata.org/numeric-operators#-addition
     */
    @Test
    fun Addition() {
        val expected = DecimalNode(BigDecimal.valueOf(7L))
        val actual = JSong.of("5 + 2").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#--substractionnegation
     */
    @Test
    fun Subtraction() {
        val expected = DecimalNode(BigDecimal(3L))
        val actual = JSong.of("5 - 2").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#--substractionnegation
     */
    @Test
    fun Negation() {
        val expected = DecimalNode(BigDecimal(-42))
        val actual = JSong.of("-42").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-multiplication
     */
    @Test
    fun Multiplication() {
        val expected = DecimalNode(BigDecimal(10L))
        val actual = JSong.of("5 * 2").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-division
     */
    @Test
    fun Division() {
        val expected = DecimalNode(BigDecimal(2.5))
        val actual = JSong.of("5 / 2").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-modulo
     */
    @Test
    fun Reminder() {
        val expected = DecimalNode(BigDecimal.ONE)
        val actual = JSong.of("5 % 2").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun Range() {
        val expected = TestResources.mapper.readTree("[1, 2, 3, 4, 5]")
        val actual = (JSong.of("[1..5]").evaluate() as RangeNode).indexes
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Range gap`() {
        val expected = TestResources.mapper.readTree("[1, 2, 3, 7, 8, 9]")
        val actual = (JSong.of("[1..3, 7..9]").evaluate() as _RangesNode).indexes
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Range expression`() {
        val expected = TestResources.mapper.readTree("[\"Item 1\",\"Item 2\",\"Item 3\"]")
        val actual = JSong.of("[1..\$count(Items)].(\"Item \" & \$)").evaluate(TestResources.items)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Range context`() {
        val expression = "[1..5].(\$ * \$)"
        val expected = TestResources.mapper.createArrayNode()
            .add(BigDecimal(1L))
            .add(BigDecimal(4L))
            .add(BigDecimal(9L))
            .add(BigDecimal(16L))
            .add(BigDecimal(25L))
        val actual = JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

}