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
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong3.JSong
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/object-functions
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestObjectFunctions {

    private val om = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = om.readTree(Thread.currentThread().contextClassLoader.getResource("invoice.json"))
    }

    /**
     * https://docs.jsonata.org/object-functions#assert
     */
    @Test
    fun `$assert - negative`() {
        val expression = "\$assert(false, \"I'm innocent\")"
        assertThrows<AssertionError> {
            JSong(expression).evaluate()
        }
    }

    /**
     * https://docs.jsonata.org/object-functions#assert
     */
    @Test
    fun `$assert - positive`() {
        val expression = "\$assert(true, \"no bug, no cries\")"
        JSong(expression).evaluate()
    }

    /**
     * https://docs.jsonata.org/object-functions#error
     */
    @Test
    fun `$error`() {
        val expression = "\$error(\"hello bug\")"
        assertThrows<Error> {
            JSong(expression).evaluate()
        }
    }

    /**
     * https://docs.jsonata.org/object-functions#keys
     */
    @Test
    fun `$keys`() {
        val expression = "\$keys(Account.Order.Product)"

        @Language("JSON")
        val expected = om.readTree(
            """
            [
              "Product Name",
              "ProductID",
              "SKU",
              "Description",
              "Price",
              "Quantity"
            ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#lookup
     */
    @Test
    fun `$lookup`() {
        val expression = "\$lookup(Account.Order.Product, \"SKU\")"
        @Language("JSON")
        val expected = om.readTree(
            """
            [
              "0406654608",
              "0406634348",
              "040657863",
              "0406654603"
            ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#merge
     *
     * THE https://try.jsonata.org/ HAS A BUG: $merge JUST RETURNS THE LAST ELEMENT IF SOME KEY ALREADY EXISTS.
     */
    @Test
    fun `$merge`() {
        val expression = "\$merge(Account.Order.Product)"
        @Language("JSON")
        val expected = om.readTree(
            """
            {
              "Product Name": "Cloak",
              "ProductID": 345664,
              "SKU": "0406654603",
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
        """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#spread
     */
    @Test
    fun `$spread`() {
        val expression = "\$spread(Account.Order.Product)"
        @Language("JSON")
        val expected = om.readTree(
            """
            [
              {
                "Product Name": "Bowler Hat"
              },
              {
                "ProductID": 858383
              },
              {
                "SKU": "0406654608"
              },
              {
                "Description": {
                  "Colour": "Purple",
                  "Width": 300,
                  "Height": 200,
                  "Depth": 210,
                  "Weight": 0.75
                }
              },
              {
                "Price": 34.45
              },
              {
                "Quantity": 2
              },
              {
                "Product Name": "Trilby hat"
              },
              {
                "ProductID": 858236
              },
              {
                "SKU": "0406634348"
              },
              {
                "Description": {
                  "Colour": "Orange",
                  "Width": 300,
                  "Height": 200,
                  "Depth": 210,
                  "Weight": 0.6
                }
              },
              {
                "Price": 21.67
              },
              {
                "Quantity": 1
              },
              {
                "Product Name": "Bowler Hat"
              },
              {
                "ProductID": 858383
              },
              {
                "SKU": "040657863"
              },
              {
                "Description": {
                  "Colour": "Purple",
                  "Width": 300,
                  "Height": 200,
                  "Depth": 210,
                  "Weight": 0.75
                }
              },
              {
                "Price": 34.45
              },
              {
                "Quantity": 4
              },
              {
                "ProductID": 345664
              },
              {
                "SKU": "0406654603"
              },
              {
                "Product Name": "Cloak"
              },
              {
                "Description": {
                  "Colour": "Black",
                  "Width": 30,
                  "Height": 20,
                  "Depth": 210,
                  "Weight": 2
                }
              },
              {
                "Price": 107.99
              },
              {
                "Quantity": 1
              }
           ]          
        """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - array`() {
        val expression = "\$type(Account.Order)"
        val expected = TextNode(ObjectFunctions.Type.array.name)
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - boolean`() {
        val expression = "\$type($)"
        val expected = TextNode(ObjectFunctions.Type.boolean.name)
        val actual = JSong(expression).evaluate(BooleanNode.TRUE)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - null`() {
        val expression = "\$type($)"
        val expected = TextNode(ObjectFunctions.Type.`null`.name)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - number`() {
        val expression = "\$type($)"
        val expected = TextNode(ObjectFunctions.Type.number.name)
        val node = DecimalNode(BigDecimal("3.14"))
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - object`() {
        val expression = "\$type($)"
        val expected = TextNode(ObjectFunctions.Type.`object`.name)
        val node = om.createObjectNode()
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - string`() {
        val expression = "\$type($)"
        val expected = TextNode(ObjectFunctions.Type.string.name)
        val node = TextNode("God's in his heaven — All's right with the world.")
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

}