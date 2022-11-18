package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/path-operators
 */
class TestPathOperators {

    /**
     * https://docs.jsonata.org/path-operators
     */
    @Test
    fun `map results in scalar`() {
        val expected = JSong.of("\"Winchester\"").evaluate()
        val actual = JSong.of("Address.City").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators
     */
    @Test
    fun `map results in array`() {
        val expected =
            JSong.of("[ \"0203 544 1234\", \"01962 001234\", \"01962 001235\", \"077 7700 1234\" ]").evaluate()
        val actual = JSong.of("Phone.number").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators
     */
    @Test
    fun `map multiplication`() {
        val expected = JSong.of("[ 68.9, 21.67, 137.8, 107.99 ]").evaluate()
        val actual = JSong.of("Account.Order.Product.(Price * Quantity)").evaluate(TestResources.invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators
     */
    @Test
    fun `map function`() {
        val expected = JSong.of("[ \"ORDER103\", \"ORDER104\"]").evaluate()
        val actual = JSong.of("Account.Order.OrderID.\$uppercase()").evaluate(TestResources.invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#---order-by
     */
    @Test
    @Disabled
    fun `order by default`() {
        @Language("JSON")
        val expected = JSong.of(
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
        ).evaluate()
        val actual = JSong.of("Account.Order.Product^(Price)").evaluate(TestResources.invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#---order-by
     */
    @Test
    @Disabled
    fun `order by decreasing`() {
        @Language("JSON")
        val expected = JSong.of(
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
        ).evaluate()
        val actual = JSong.of("Account.Order.Product^(>Price)").evaluate(TestResources.invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#---order-by
     */
    @Test
    @Disabled
    fun `order by decreasing price, increasing quantity`() {
        @Language("JSON")
        val expected = JSong.of(
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
        ).evaluate()
        val actual = JSong.of("Account.Order.Product^(>Price, <Quantity)").evaluate(TestResources.invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#---order-by
     */
    @Test
    @Disabled
    fun `order by increasing total`() {
        @Language("JSON")
        val expected = JSong.of(
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
        ).evaluate()
        val actual = JSong.of("Account.Order.Product^(Price * Quantity)").evaluate(TestResources.invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#-positional-variable-binding
     */
    @Test
    fun `Positional variable binding`() {
        val expression = "library.books#\$i[\"Kernighan\" in authors].{\"title\": title, \"index\": \$i }"

        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [
              {
                "title": "The C Programming Language",
                "index": 1
              },
              {
                "title": "The AWK Programming Language",
                "index": 2
              }
            ]
            """.trimIndent()
        )
        val actual = JSong.of(expression).evaluate(TestResources.library)
        assertEquals(expected, actual)
    }


    @Test
    fun `Context variable binding - carry on once`() {
        val expression = "library.loans@\$L"
        val actual = JSong.of(expression).evaluate(TestResources.library)
        val library = JSong.of("library").evaluate(TestResources.library)
        val loans = JSong.of("library.loans").evaluate(TestResources.library)
        val expected = TestResources.mapper.createArrayNode().let {
            for (i in 1..loans!!.size()) {
                it.add(library)
            }
            it
        }
        assertEquals(expected, actual)
    }

    @Test
    fun `Context variable binding - carry on once and recall`() {
        val expression = "library.loans@\$L.{\"loan\": \$L}"
        val actual = JSong.of(expression).evaluate(TestResources.library)
        val loans = JSong.of("library.loans").evaluate(TestResources.library)
        val expected = TestResources.mapper.createArrayNode().let {
            for (i in 0 until loans!!.size()) {
                it.addObject().set<JsonNode>("loan", loans[i])
            }
            it
        }
        assertEquals(expected, actual)
    }

    @Test
    fun `Context variable binding - carry on twice`() {
        val expression = "library.loans@\$L.books@\$B"
        val actual = JSong.of(expression).evaluate(TestResources.library)
        val library = JSong.of("library").evaluate(TestResources.library)
        val loans = JSong.of("library.loans").evaluate(TestResources.library)
        val books = JSong.of("library.books").evaluate(TestResources.library)
        val expected = TestResources.mapper.createArrayNode().let {
            for (i in 1..loans!!.size() * books!!.size()) {
                it.add(library)
            }
            it
        }
        assertEquals(expected, actual)
    }

    @Test
    fun `Context variable binding - carry on twice and recall`() {
        val expression = "library.loans@\$L.books@\$B.{\"title\": \$B.title}"
        val actual = JSong.of(expression).evaluate(TestResources.library)
        val loans = JSong.of("library.loans").evaluate(TestResources.library)
        val titles = JSong.of("library.books.title").evaluate(TestResources.library)
        val expected = TestResources.mapper.createArrayNode().let {
            for (i in 1..loans!!.size()) {
                titles!!.forEach { title ->
                    it.addObject().set<JsonNode>("title", title)
                }
            }
            it
        }
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#-context-variable-binding
     */
    @Test
    fun `Context variable binding - join`() {
        // library.loans@$l.books@$b[$l.isbn=$b.isbn].{"title": $b.title}
        val expression =
            "library.loans@\$L.books@\$B[\$L.isbn=\$B.isbn].{\"title\": \$B.title, \"customer\": \$L.customer}"

        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [
              {
                "title": "Structure and Interpretation of Computer Programs",
                "customer": "10001"
              },
              {
                "title": "Compilers: Principles, Techniques, and Tools",
                "customer": "10003"
              },
              {
                "title": "Structure and Interpretation of Computer Programs",
                "customer": "10003"
              }
            ]   
            """.trimIndent()
        )
        val actual = JSong.of(expression).evaluate(TestResources.library)
        assertEquals(expected, actual)
    }

}


