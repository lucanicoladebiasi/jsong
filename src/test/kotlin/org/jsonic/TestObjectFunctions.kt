package org.jsonic

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
        val expected = TestResources.mapper.readTree(
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
        val actual = Processor(TestResources.invoice).evaluate("\$keys(Account.Order.Product)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#lookup
     */
    @Test
    fun `$lookup`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [
              "0406654608",
              "0406634348",
              "040657863",
              "0406654603"
            ]
            """.trimIndent()
        )
        val actual = Processor(TestResources.invoice).evaluate("\$lookup(Account.Order.Product, \"SKU\")")
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
        val expected = TestResources.mapper.readTree(
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
        val actual = Processor(TestResources.invoice).evaluate("\$merge(Account.Order.Product)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#spread
     */
    @Test
    fun `$spread`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
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
        val actual = Processor(TestResources.invoice).evaluate("\$spread(Account.Order.Product)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - array`() {
        val expected =Library.Type.ARRAY.descriptor
        val actual = Processor(TestResources.mapper.readTree("[]")).evaluate("\$type($)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - boolean`() {
        val expected = Library.Type.BOOLEAN.descriptor
        val actual = Processor(TestResources.mapper.readTree("true")).evaluate("\$type($)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - null`() {
        val expected = Library.Type.NULL.descriptor
        val actual = Processor(TestResources.mapper.readTree("null")).evaluate("\$type($)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - number`() {
        val expected = Library.Type.NUMBER.descriptor
        val actual = Processor(TestResources.mapper.readTree("3.14")).evaluate("\$type($)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - object`() {
        val expected = Library.Type.OBJECT.descriptor
        val actual = Processor(TestResources.mapper.readTree("{}")).evaluate("\$type($)")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - string`() {
        val expected = Library.Type.STRING.descriptor
        val actual = Processor(
            TestResources.mapper.readTree("\"God's in his heaven — All's right with the world.\"")
        ).evaluate("\$type($)")
        assertEquals(expected, actual)
    }

}