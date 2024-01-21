package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.IntNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestArrayType {

    private val om = ObjectMapper()

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
        val expected = om.readTree(expression)
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
        val expected = om.createArrayNode().addAll((min..max).map { IntNode(it) })
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    @Test
    fun `Range - twisted`() {
        val max = 3.14159265359
        val min = 2.718281828459
        val expression = "[$max..$min]"
        val expected = om.createArrayNode().addAll((min.toInt() ..max.toInt()).map { IntNode(it) })
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
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
        val expected = om.createArrayNode()
        expected.addAll((lmin .. lmax).map { IntNode(it) })
        expected.addAll((rmin .. rmax).map { IntNode(it) })
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
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
        val expected =om.createArrayNode()
        expected.addAll((lmin .. lmax).map { IntNode(it) })
        expected.addAll((rmin .. rmax).map { IntNode(it) })
        expected.addAll((omin .. omax).map { IntNode(it) })
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}