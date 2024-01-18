/**
 * MIT License
 *
 * Copyright (c) 2023 Luca Nicola Debiasi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import io.github.lucanicoladebiasi.jsong3.JSong
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/array-functions
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestArrayFunctions {

    private val om = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = om.readTree(Thread.currentThread().contextClassLoader.getResource("invoice.json"))
    }

    /**
     * https://docs.jsonata.org/array-functions#count
     */
    @Test
    fun `$count() array`() {
        val expression = "\$count([1,2,3,1])"
        val expected = DecimalNode(4.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#count
     */
    @Test
    fun `$count singleton`() {
        val expression = "\$count(\"hello\")"
        val expected = DecimalNode(1.toBigDecimal())
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @Test
    fun `$append() - array`() {
        val expression = "\$append([1,2,3], [4,5,6])"

        @Language("JSON")
        val expected = om.createArrayNode().addAll(
            om.readTree(
                """
            [1,2,3,4,5,6]
            """.trimIndent()
            ).map {
                DecimalNode(it.decimalValue())
            }
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @Test
    fun `$append() - singleton`() {
        val expression = "\$append([1,2,3], 4)"

        @Language("JSON")
        val expected = om.createArrayNode().addAll(
            om.readTree(
                """
              [1,2,3,4]
                """.trimIndent()
            ).map {
                DecimalNode(it.decimalValue())
            }
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @Test
    fun `$append() - text`() {
        val expression = "\$append(\"Hello\", \"World\")"

        @Language("JSON")
        val expected = om.readTree(
            """
            ["Hello", "World"]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#sort
     */
    @Test
    fun `$sort() - default`() {
        val expression = "\$sort(Account.Order.Product.Description.Weight)"

        @Language("JSON")
        val expected = om.readTree(
            """
            [0.6, 0.75, 0.75, 2]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#sort
     */
    @Test
    fun `$sort() - function`() {
        val expression = "\$sort(Account.Order.Product, fun(\$l, \$r){\$l.Description.Weight > \$r.Description.Weight})"

        @Language("JSON")
        val expected = om.readTree(
            """
            [
              {
                "Product Name": "Trilby hat",
                "ProductID": 858236,
                "SKU": "0406634348",
                "Description": {
                  "Colour": "Orange",
                  "Width": 300,
                  "Height": 200,
                  "Depth": 210,
                  "Weight": 0.6
                },
                "Price": 21.67,
                "Quantity": 1
              },
                 {
                "Product Name": "Bowler Hat",
                "ProductID": 858383,
                "SKU": "040657863",
                "Description": {
                  "Colour": "Purple",
                  "Width": 300,
                  "Height": 200,
                  "Depth": 210,
                  "Weight": 0.75
                },
                "Price": 34.45,
                "Quantity": 4
              },
              {
                "Product Name": "Bowler Hat",
                "ProductID": 858383,
                "SKU": "0406654608",
                "Description": {
                  "Colour": "Purple",
                  "Width": 300,
                  "Height": 200,
                  "Depth": 210,
                  "Weight": 0.75
                },
                "Price": 34.45,
                "Quantity": 2
              },
              {
                "ProductID": 345664,
                "SKU": "0406654603",
                "Product Name": "Cloak",
                "Description": {
                  "Colour": "Black",
                  "Width": 30,
                  "Height": 20,
                  "Depth": 210,
                  "Weight": 2
                },
                "Price": 107.99,
                "Quantity": 1
              }
            ]
        """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#reverse
     */
    @Test
    fun `$reverse() - argument`() {
        val expression = "\$reverse([\"Hello\", \"World\"])"

        @Language("JSON")
        val expected = om.readTree(
            """
            ["World", "Hello"]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#reverse
     */
    @Test
    @Disabled
    fun `$reverse() - context`() {
        val expression = "[1..5] ~> \$reverse()"

        @Language("JSON")
        val expected = om.readTree(
            """
            [5, 4, 3, 2, 1]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }


    /**
     * https://docs.jsonata.org/array-functions#shuffle
     */
    @Test
    fun `$shuffle()`() {
        val expression = "\$shuffle([1..9])"
        val actual = JSong(expression).evaluate()
        assertTrue(actual is ArrayNode)
        assertEquals(9, actual!!.size())
        for (i in 1..9) {
            assertTrue(actual.contains(IntNode(i)))
        }
    }

    /**
     * https://docs.jsonata.org/array-functions#distinct
     */
    @Test
    fun `$distinct() - numeric`() {
        val expression = "\$distinct([1,2,3,3,4,3,5])"

        @Language("JSON")
        val expected = om.createArrayNode().addAll(
            om.readTree(
            """
            [1, 2, 3, 4, 5]
            """.trimIndent()
            ).map {
                DecimalNode(it.decimalValue())
            }
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#distinct
     */
    @Test
    fun `$distinct() - path`() {
        val expression = "\$distinct(Account.Order.Product.Description.Colour)"

        @Language("JSON")
        val expected = om.readTree(
            """
            [ "Purple", "Orange", "Black" ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#zip
     */
    @Test
    @Disabled
    fun `$zip() - pair`() {
        val expression = "\$zip([1,2,3], [4,5,6])"

        @Language("JSON")
        val expected = om.readTree(
            """
            [[1,4] ,[2,5], [3,6]]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#zip
     */
    @Test
    @Disabled
    fun `$zip() - triplet`() {
        val expression = "\$zip([1,2,3],[4,5],[7,8,9])"

        @Language("JSON")
        val expected = om.readTree(
            """
            [[1,4,7], [2,5,8]]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}