package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
        val expected = (min..max).toSet()
        val actual = (JSong(expression).evaluate() as RangeNode).indexes
        assertEquals(expected, actual)
    }

    @Test
    fun `Range - twisted`() {
        val max = 3.14159265359
        val min = 2.718281828459
        val expression = "[$max..$min]"
        val expected = (min.toInt() ..max.toInt()).toSet()
        val actual = (JSong(expression).evaluate() as RangeNode).indexes
        assertEquals(expected, actual.sorted().toSet())
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
        val actual = mutableSetOf<Int>()
        JSong(expression).evaluate()?.filterIsInstance<RangeNode>()?.map { actual.addAll(it.indexes) }
        assertEquals(expected, actual.sorted().toSet())
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
        val actual = mutableSetOf<Int>()
        JSong(expression).evaluate()?.filterIsInstance<RangeNode>()?.map { actual.addAll(it.indexes) }
        assertEquals(expected, actual.sorted().toSet())
    }

}