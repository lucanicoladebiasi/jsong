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
package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/path-operators
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestOrderByOperators {

    private val mapper = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapper.readTree(Thread.currentThread().contextClassLoader.getResource("invoice.json"))
    }

    /**
     * https://docs.jsonata.org/path-operators#---order-by
     */
    @Test
    @Disabled
    fun `order by default`() {
        val expression = "Account.Order.Product^(Price)"
        @Language("JSON")
        val expected = mapper.readTree(
            """
                [
                    {
                        "Product Name":"Trilby hat",
                        "ProductID":858236,"SKU":"0406634348",
                        "Description":  {
                            "Colour":"Orange",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.6
                        },
                        "Price":21.67,
                        "Quantity":1
                    },
                    {
                        "Product Name":"Bowler Hat",
                        "ProductID":858383,
                        "SKU":"0406654608",
                        "Description":  {
                            "Colour":"Purple",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.75
                        },
                        "Price":34.45,
                        "Quantity":2
                    },
                    {   "Product Name":"Bowler Hat",
                        "ProductID":858383,
                        "SKU":"040657863",
                        "Description":  {
                            "Colour":"Purple",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.75
                        },
                        "Price":34.45,
                        "Quantity":4
                    },
                    {
                        "ProductID":345664,
                        "SKU":"0406654603",
                        "Product Name":"Cloak",
                        "Description":  {
                            "Colour":"Black",
                            "Width":30,
                            "Height":20,
                            "Depth":210,
                            "Weight":2
                        },
                        "Price":107.99,
                        "Quantity":1
                    }
                ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#---order-by
     */
    @Test
    @Disabled
    fun `order by decreasing`() {
        val expression = "Account.Order.Product^(>Price)"
        @Language("JSON")
        val expected = mapper.readTree(
            """
                [
                {
                        "ProductID":345664,
                        "SKU":"0406654603",
                        "Product Name":"Cloak",
                        "Description":  {
                            "Colour":"Black",
                            "Width":30,
                            "Height":20,
                            "Depth":210,
                            "Weight":2
                        },
                        "Price":107.99,
                        "Quantity":1
                    },
                    {
                        "Product Name":"Bowler Hat",
                        "ProductID":858383,
                        "SKU":"0406654608",
                        "Description":  {
                            "Colour":"Purple",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.75
                        },
                        "Price":34.45,
                        "Quantity":2
                    },
                    {   
                        "Product Name":"Bowler Hat",
                        "ProductID":858383,
                        "SKU":"040657863",
                        "Description":  {
                            "Colour":"Purple",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.75
                        },
                        "Price":34.45,
                        "Quantity":4
                    },
                    {
                        "Product Name":"Trilby hat",
                        "ProductID":858236,"SKU":"0406634348",
                        "Description":  {
                            "Colour":"Orange",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.6
                        },
                        "Price":21.67,
                        "Quantity":1
                    }
                    
                ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#---order-by
     */
    @Test
    @Disabled
    fun `order by decreasing price, increasing quantity`() {
        val expression = "Account.Order.Product^(>Price, <Quantity)"
        @Language("JSON")
        val expected = mapper.readTree(
            """
                [
                {
                        "ProductID":345664,
                        "SKU":"0406654603",
                        "Product Name":"Cloak",
                        "Description":  {
                            "Colour":"Black",
                            "Width":30,
                            "Height":20,
                            "Depth":210,
                            "Weight":2
                        },
                        "Price":107.99,
                        "Quantity":1
                    },
                    {
                        "Product Name":"Bowler Hat",
                        "ProductID":858383,
                        "SKU":"0406654608",
                        "Description":  {
                            "Colour":"Purple",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.75
                        },
                        "Price":34.45,
                        "Quantity":2
                    },
                    {   
                        "Product Name":"Bowler Hat",
                        "ProductID":858383,
                        "SKU":"040657863",
                        "Description":  {
                            "Colour":"Purple",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.75
                        },
                        "Price":34.45,
                        "Quantity":4
                    },
                    {
                        "Product Name":"Trilby hat",
                        "ProductID":858236,"SKU":"0406634348",
                        "Description":  {
                            "Colour":"Orange",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.6
                        },
                        "Price":21.67,
                        "Quantity":1
                    }
                    
                ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#---order-by
     */
    @Test
    @Disabled
    fun `order by increasing total`() {
        val expression = "Account.Order.Product^(Price * Quantity)"
        @Language("JSON")
        val expected = mapper.readTree(
            """
                [
                    {
                        "Product Name":"Trilby hat",
                        "ProductID":858236,"SKU":"0406634348",
                        "Description":  {
                            "Colour":"Orange",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.6
                        },
                        "Price":21.67,
                        "Quantity":1
                    },                  
                    {
                        "Product Name":"Bowler Hat",
                        "ProductID":858383,
                        "SKU":"0406654608",
                        "Description":  {
                            "Colour":"Purple",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.75
                        },
                        "Price":34.45,
                        "Quantity":2
                    },
                    {
                        "ProductID":345664,
                        "SKU":"0406654603",
                        "Product Name":"Cloak",
                        "Description":  {
                            "Colour":"Black",
                            "Width":30,
                            "Height":20,
                            "Depth":210,
                            "Weight":2
                        },
                        "Price":107.99,
                        "Quantity":1
                    }, 
                    {   
                        "Product Name":"Bowler Hat",
                        "ProductID":858383,
                        "SKU":"040657863",
                        "Description":  {
                            "Colour":"Purple",
                            "Width":300,
                            "Height":200,
                            "Depth":210,
                            "Weight":0.75
                        },
                        "Price":34.45,
                        "Quantity":4
                    }                                                     
                ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

}


