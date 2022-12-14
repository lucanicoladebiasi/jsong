package org.jsong

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
        assertThrows<Error> { JSong.of("\$assert(false, \"I'm innocent\")").evaluate() }
    }

    /**
     * https://docs.jsonata.org/object-functions#assert
     */
    @Test
    fun `$assert - positive`() {
        JSong.of("\$assert(true, \"no bug, no cries\")").evaluate()
    }

    /**
     * https://docs.jsonata.org/object-functions#error
     */
    fun `$error`() {
        assertThrows<Error> { JSong.of("\$error(\"hello bug\")").evaluate() }
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
        val actual = JSong.of("\$keys(Account.Order.Product)").evaluate(TestResources.invoice)
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
        val actual = JSong.of("\$lookup(Account.Order.Product, \"SKU\")").evaluate(TestResources.invoice)
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
               "Product Name":[
                  "Bowler Hat",
                  "Trilby hat",
                  "Bowler Hat",
                  "Cloak"
               ],
               "ProductID":[
                  858383,
                  858236,
                  858383,
                  345664
               ],
               "SKU":[
                  "0406654608",
                  "0406634348",
                  "040657863",
                  "0406654603"
               ],
               "Description":[
                  {
                     "Colour":"Purple",
                     "Width":300,
                     "Height":200,
                     "Depth":210,
                     "Weight":0.75
                  },
                  {
                     "Colour":"Orange",
                     "Width":300,
                     "Height":200,
                     "Depth":210,
                     "Weight":0.6
                  },
                  {   
                     "Colour":"Purple",
                     "Width":300,
                     "Height":200,
                     "Depth":210,
                     "Weight":0.75
                  },
                  {  
                      "Colour":"Black",
                      "Width":30,
                      "Height":20,
                      "Depth":210,
                      "Weight":2
                  }
               ],
               "Price":[
                  34.45,
                  21.67,
                  34.45,
                  107.99
               ],
               "Quantity":[
                 2,
                 1,
                 4,
                 1
               ]
            }
        """.trimIndent()
        )
        val actual = JSong.of("\$merge(Account.Order.Product)").evaluate(TestResources.invoice)
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
        val actual = JSong.of("\$spread(Account.Order.Product)").evaluate(TestResources.invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - array`() {
        val expected = Functions.Type.ARRAY.descriptor
        val actual = JSong.of("\$type($)").evaluate(TestResources.mapper.readTree("[]"))
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - boolean`() {
        val expected = Functions.Type.BOOLEAN.descriptor
        val actual = JSong.of("\$type($)").evaluate(TestResources.mapper.readTree("true"))
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - null`() {
        val expected = Functions.Type.NULL.descriptor
        val actual = JSong.of("\$type($)").evaluate(TestResources.mapper.readTree("null"))
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - number`() {
        val expected = Functions.Type.NUMBER.descriptor
        val actual = JSong.of("\$type($)").evaluate(TestResources.mapper.readTree(("3.14")))
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - object`() {
        val expected = Functions.Type.OBJECT.descriptor
        val actual = JSong.of("\$type($)").evaluate(TestResources.mapper.readTree(("{}")))
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @Test
    fun `$type - string`() {
        val expected = Functions.Type.STRING.descriptor
        val actual = JSong.of("\$type($)")
            .evaluate(TestResources.mapper.readTree(("\"God's in his heaven ??? All's right with the world.\"")))
        assertEquals(expected, actual)
    }

}