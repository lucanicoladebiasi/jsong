package org.jsonic

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