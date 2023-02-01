package org.jsong

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/array-functions
 */
class TestArrayFunctions {
    
    /**
     * https://docs.jsonata.org/array-functions#count
     */
    @Test
    fun `$count() array`() {
        val expected = Processor().evaluate("4")
        val actual = Processor().evaluate("\$count([1,2,3,1])")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#count
     */
    @Test
    fun `$count singleton`() {
        val expected = Processor().evaluate("1")
        val actual = Processor().evaluate("\$count(\"hello\")")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @Test
    fun `$append() - array`() {
        val expected = Processor().evaluate("[1,2,3,4,5,6]")
        val actual = Processor().evaluate("\$append([1,2,3], [4,5,6])")
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @Test
    fun `$append() - singleton`() {
        val expected = Processor().evaluate("[1,2,3,4]")
        val actual = Processor().evaluate("\$append([1,2,3], 4)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @Test
    fun `$append() - text`() {
        val expected = Processor().evaluate("[\"Hello\", \"World\"]")
        val actual = Processor().evaluate("\$append(\"Hello\", \"World\")")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#sort
     */
    @Disabled("todo: functions")
    @Test
    fun `$sort()`() {
        val expression = """
            ${'$'}sort(Account.Order.Product, function(${'$'}l, ${'$'}) {
              ${'$'}l.Description.Weight > ${'$'}r.Description.Weight
            })
        """.trimIndent()
        val expected = Processor().evaluate("")
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#reverse
     */
    @Test
    fun `$reverse() - argument`() {
        val expected = Processor().evaluate("[\"World\", \"Hello\"]")
        val actual = Processor().evaluate("\$reverse([\"Hello\", \"World\"])")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#reverse
     */
    @Disabled("to do ~> operator")
    @Test
    fun `$reverse() - context`() {
        val expected = Processor().evaluate("[5, 4, 3, 2, 1]")
        val actual = Processor().evaluate("[1..5] ~> \$reverse()")
        assertEquals(expected, actual)
    }


    /**
     * https://docs.jsonata.org/array-functions#shuffle
     */
    @Test
    fun `$shuffle()`() {
        val actual = Processor().evaluate("\$shuffle([1..9])")
        assertTrue(actual is ArrayNode)
        assertEquals(9, actual!!.size())
        for(i in 1..9) {
            assertTrue(actual.contains(IntNode(i)))
        }
    }

    /**
     * https://docs.jsonata.org/array-functions#distinct
     */
    @Test
    fun `$distinct() - numeric`() {
        val expected = Processor().evaluate("[1, 2, 3, 4, 5]")
        val actual = Processor().evaluate("\$distinct([1,2,3,3,4,3,5])")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#distinct
     */
    @Test
    fun `$distinct() - path`() {
        val expected = Processor().evaluate("[ \"Purple\", \"Orange\", \"Black\" ]")
        val actual = Processor(TestResources.invoice).evaluate("\$distinct(Account.Order.Product.Description.Colour)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#zip
     */
    @Disabled("todo: zip function")
    @Test
    fun `$zip() - pair`() {
        val expected = Processor().evaluate("[[1,4] ,[2,5], [3,6]]")
        val actual = Processor().evaluate("\$zip([1,2,3], [4,5,6])")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#zip
     */
    @Disabled("todo: zip function")
    @Test
    fun `$zip() - triplet`() {
        val expected = Processor().evaluate("[[1,4,7], [2,5,8]]")
        val actual = Processor().evaluate("\$zip([1,2,3],[4,5],[7,8,9])")
        assertEquals(expected, actual)
    }

}