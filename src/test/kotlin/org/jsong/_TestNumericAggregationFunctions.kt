package org.jsong

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/aggregation-functions
 */
class _TestNumericAggregationFunctions {

    /**
     * https://docs.jsonata.org/aggregation-functions#sum
     */
    @Test
    fun `$sum()`() {
        val expected = _JSong.of("20").evaluate()
        val actual = _JSong.of("\$sum([5,1,3,7,4])").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/aggregation-functions#max
     */
    @Test
    fun `$max()`() {
        val expected = _JSong.of("7").evaluate()
        val actual = _JSong.of("\$max([5,1,3,7,4])").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/aggregation-functions#min
     */
    @Test
    fun `$min()`() {
        val expected = _JSong.of("1").evaluate()
        val actual = _JSong.of("\$min([5,1,3,7,4])").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/aggregation-functions#average
     */
    @Test
    fun `$average()`() {
        val expected = _JSong.of("4").evaluate()
        val actual = _JSong.of("\$average([5,1,3,7,4])").evaluate()
        assertEquals(expected, actual)
    }

}