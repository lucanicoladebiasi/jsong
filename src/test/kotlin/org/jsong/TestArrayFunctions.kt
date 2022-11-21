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
        val expected = JSong.of("4").evaluate()
        val actual = JSong.of("\$count([1,2,3,1])").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#count
     */
    @Test
    fun `$count singleton`() {
        val expected = JSong.of("1").evaluate()
        val actual = JSong.of("\$count(\"hello\")").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @Test
    fun `$append() - array`() {
        val expected = JSong.of("[1,2,3,4,5,6]").evaluate()
        val actual = JSong.of("\$append([1,2,3], [4,5,6])").evaluate()
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @Test
    fun `$append() - singleton`() {
        val expected = JSong.of("[1,2,3,4]").evaluate()
        val actual = JSong.of("\$append([1,2,3], 4)").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @Test
    fun `$append() - text`() {
        val expected = JSong.of("[\"Hello\", \"World\"]").evaluate()
        val actual = JSong.of("\$append(\"Hello\", \"World\")").evaluate()
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
        val expected = JSong.of("").evaluate()
        val actual = JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#reverse
     */
    @Test
    fun `$reverse() - argument`() {
        val expected = JSong.of("[\"World\", \"Hello\"]").evaluate()
        val actual = JSong.of("\$reverse([\"Hello\", \"World\"])").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#reverse
     */
    @Disabled("to do ~> operator")
    @Test
    fun `$reverse() - context`() {
        val expected = JSong.of("[5, 4, 3, 2, 1]").evaluate()
        val actual = JSong.of("[1..5] ~> \$reverse()").evaluate()
        assertEquals(expected, actual)
    }


    /**
     * https://docs.jsonata.org/array-functions#shuffle
     */
    @Test
    fun `$shuffle()`() {
        val actual = JSong.of("\$shuffle([1..9])").evaluate()
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
        val expected = JSong.of("[1, 2, 3, 4, 5]").evaluate()
        val actual = JSong.of("\$distinct([1,2,3,3,4,3,5])").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#distinct
     */
    @Test
    fun `$distinct() - path`() {
        val expected = JSong.of("[ \"Purple\", \"Orange\", \"Black\" ]").evaluate()
        val actual = JSong.of("\$distinct(Account.Order.Product.Description.Colour)").evaluate(TestResources.invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#zip
     */
    @Disabled("todo: zip function")
    @Test
    fun `$zip() - pair`() {
        val expected = JSong.of("[[1,4] ,[2,5], [3,6]]").evaluate()
        val actual = JSong.of("\$zip([1,2,3], [4,5,6])").evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#zip
     */
    @Disabled("todo: zip function")
    @Test
    fun `$zip() - triplet`() {
        val expected = JSong.of("[[1,4,7], [2,5,8]]").evaluate()
        val actual = JSong.of("\$zip([1,2,3],[4,5],[7,8,9])").evaluate()
        assertEquals(expected, actual)
    }

}