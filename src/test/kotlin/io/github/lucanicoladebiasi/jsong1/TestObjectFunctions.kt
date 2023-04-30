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
package io.github.lucanicoladebiasi.jsong1

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/object-functions
 */
class TestObjectFunctions {
    
    /**
     * https://docs.jsonata.org/object-functions#assert
     */
    @Test
    fun `$assert - negative`() {
        assertThrows<AssertionError> { Processor().evaluate("\$assert(false, \"I'm innocent\")") }
    }

    /**
     * https://docs.jsonata.org/object-functions#assert
     */
    @Test
    fun `$assert - positive`() {
        Processor().evaluate("\$assert(true, \"no bug, no cries\")")
    }

    /**
     * https://docs.jsonata.org/object-functions#error
     */
    @Test
    fun `$error`() {
        assertThrows<Error> { Processor().evaluate("\$error(\"hello bug\")") }
    }

    /**
     * https://docs.jsonata.org/object-functions#keys
     */
    @Test
    fun `$keys`() {
        @Language("JSON")
        val expected = TestResources1.mapper.readTree(
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
        val actual = Processor(TestResources1.invoice).evaluate("\$keys(Account.Order.Product)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#lookup
     */
    @Test
    fun `$lookup`() {
        @Language("JSON")
        val expected = TestResources1.mapper.readTree(
            """
            [
              "0406654608",
              "0406634348",
              "040657863",
              "0406654603"
            ]
            """.trimIndent()
        )
        val actual = Processor(TestResources1.invoice).evaluate("\$lookup(Account.Order.Product, \"SKU\")")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#merge
     *
     * THE https://try.jsonata.org/ HAS A BUG: $merge JUST RETURNS THE LAST ELEMENT IF SOME KEY ALREADY EXISTS.
     */
    @Test
    fun `$merge`() {
        @Language("JSON")
        val expected = TestResources1.mapper.readTree(
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
        val actual = Processor(TestResources1.invoice).evaluate("\$merge(Account.Order.Product)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#spread
     */
    @Test
    fun `$spread`() {
        @Language("JSON")
        val expected = TestResources1.mapper.readTree(
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
        val actual = Processor(TestResources1.invoice).evaluate("\$spread(Account.Order.Product)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - array`() {
        val expected = Library.IS_ARRAY
        val actual = Processor(TestResources1.mapper.readTree("[]")).evaluate("\$type($)")
        assertEquals(expected, actual?.textValue())
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - boolean`() {
        val expected = Library.IS_BOOLEAN
        val actual = Processor(TestResources1.mapper.readTree("true")).evaluate("\$type($)")
        assertEquals(expected, actual?.textValue())
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - null`() {
        val expected = Library.IS_NULL
        val actual = Processor(TestResources1.mapper.readTree("null")).evaluate("\$type($)")
        assertEquals(expected, actual?.textValue())
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - number`() {
        val expected = Library.IS_NUMBER
        val actual = Processor(TestResources1.mapper.readTree("3.14")).evaluate("\$type($)")
        assertEquals(expected, actual?.textValue())
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - object`() {
        val expected = Library.IS_OBJECT
        val actual = Processor(TestResources1.mapper.readTree("{}")).evaluate("\$type($)")
        assertEquals(expected, actual?.textValue())
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - string`() {
        val expected = Library.IS_STRING
        val actual = Processor(
            TestResources1.mapper.readTree("\"God's in his heaven — All's right with the world.\"")
        ).evaluate("\$type($)")
        assertEquals(expected, actual?.textValue())
    }

}