/**
 * MIT License
 *
 * Copyright (c) [2023] [Luca Nicola Debiasi]
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
package io.github.lucanicoladebiasi.jsong

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
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
    @Test
    fun `$sort() - default`() {
        val expression = "\$sort(Account.Order.Product.Description.Weight)"
        val expected = Processor().objectMapper.createArrayNode()
            .add(0.6)
            .add(0.75)
            .add(0.75)
            .add(2)
        val actual = Processor(TestResources.invoice).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#sort
     */
    @Test
    fun `$sort() - function`() {
        val expression = "\$sort(Account.Order.Product, fun(\$l, \$r){\$l.Description.Weight > \$r.Description.Weight})"

        @Language("JSON")
        val expected = Processor().objectMapper.readTree(
            """
            [
              {
                "Product Name": "Trilby hat",
                "ProductID":  858236,
                "SKU":  "0406634348",
                "Description": {
                  "Colour": "Orange",
                  "Width":  300,
                  "Height": 200,
                  "Depth":  210,
                  "Weight": 0.6
              },
              "Price":  21.67,
              "Quantity": 1
            },
            {
              "Product Name": "Bowler Hat",
              "ProductID":  858383,
              "SKU":  "0406654608",
              "Description": {
                "Colour": "Purple",
                "Width":  300,
                "Height": 200,
                "Depth":  210,
                "Weight": 0.75},
                "Price":  34.45,
                "Quantity": 2
            },
            {
              "Product Name": "Bowler Hat",
              "ProductID":  858383,
              "SKU":  "040657863",
              "Description":  {
                "Colour": "Purple",
                "Width":  300,
                "Height": 200,
                "Depth":  210,
                "Weight": 0.75
              },
              "Price":  34.45,
              "Quantity": 4
          },
          {
            "ProductID":  345664,
            "SKU":  "0406654603",
            "Product Name": "Cloak",
            "Description":  {
              "Colour": "Black",
              "Width":  30,
              "Height": 20,
              "Depth":  210,
              "Weight": 2
            },
            "Price":  107.99,
            "Quantity": 1
          }
        ]
        """.trimIndent()
        )
        val actual = Processor(TestResources.invoice).evaluate(expression)
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
    @Test
    fun `$reverse() - context`() {
        val expected = TestResources.mapper.createArrayNode().add(5).add(4).add(3).add(2).add(1)
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
        for (i in 1..9) {
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
    @Test
    fun `$zip() - pair`() {
        val expected = Processor().evaluate("[[1,4] ,[2,5], [3,6]]")
        val actual = Processor().evaluate("\$zip([1,2,3], [4,5,6])")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/array-functions#zip
     */
    @Test
    fun `$zip() - triplet`() {
        val expected = Processor().evaluate("[[1,4,7], [2,5,8]]")
        val actual = Processor().evaluate("\$zip([1,2,3],[4,5],[7,8,9])")
        assertEquals(expected, actual)
    }

}