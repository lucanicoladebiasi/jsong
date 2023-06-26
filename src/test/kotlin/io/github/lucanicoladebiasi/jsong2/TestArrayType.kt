package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.DecimalNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TestArrayType {

    private val mapper = ObjectMapper()

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Array - exp`() {
        @Language("JSON")
        val expression =
            """
            [
              "value1",
              "value2"
            ]
            """
        val expected = mapper.readTree(expression)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Array - range`() {
        val min = 1
        val max = 3
        val expression = "[$min..$max]"
        val actual = JSong(expression).evaluate()
        assertTrue(actual is RangeNode)
        actual as RangeNode
        assertEquals(DecimalNode(min.toBigDecimal()), actual.min)
        assertEquals(DecimalNode(max.toBigDecimal()), actual.max)
        assertEquals((min..max).toSet(), actual.indexes)
    }

    @Test
    fun `Range - twisted`() {
        val max = 3.14159265359
        val min = 2.718281828459
        val expression = "[$max..$min]"
        val actual = JSong(expression).evaluate()
        assertTrue(actual is RangeNode)
        actual as RangeNode
        assertEquals(DecimalNode(min.toBigDecimal()), actual.min)
        assertEquals(DecimalNode(max.toBigDecimal()), actual.max)
        assertEquals((min.toInt() ..max.toInt()).toSet(), actual.indexes)
    }


    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Literal - range with gap`() {
        val lmin = 1
        val lmax = 3
        val rmin = 5
        val rmax = 7
        val expression = "[$lmin..$lmax, $rmin..$rmax]"
        val expected = mutableSetOf<Int>()
        expected.addAll((lmin .. lmax).toSet())
        expected.addAll((rmin .. rmax).toSet())
        val actual = JSong(expression).process()
        assertEquals(expected, actual.indexes)
    }

    /**
     * https://docs.jsonata.org/numeric-operators#-range
     */
    @Test
    fun `Literal - range with overlapping gap`() {
        val lmin = 1
        val lmax = 3
        val rmin = 5
        val rmax = 7
        val omin = 2
        val omax = 4
        val expression = "[$lmin..$lmax, $rmin..$rmax, $omin..$omax]"
        val expected = mutableSetOf<Int>()
        expected.addAll((lmin .. lmax).toSet())
        expected.addAll((rmin .. rmax).toSet())
        expected.addAll((omin .. omax).toSet())
        val actual = JSong(expression).process()
        assertEquals(expected, actual.indexes)
    }

}