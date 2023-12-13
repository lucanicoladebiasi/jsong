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
package io.github.lucanicoladebiasi.jsong3

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
class TestBindOperators {

    private val mapper = ObjectMapper()

    private lateinit var node: JsonNode

    @BeforeAll
    fun setUp() {
        node = mapper.readTree(Thread.currentThread().contextClassLoader.getResource("library.json"))
    }

    /**
     * https://docs.jsonata.org/path-operators#-positional-variable-binding
     */
    @Test
    @Disabled
    fun `Positional variable binding`() {
        val expression = "library.books#\$I[\"Kernighan\" in authors].{\"title\": title, \"index\": \$I }"

        @Language("JSON")
        val expected = mapper.readTree(
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
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }


    @Test
    @Disabled
    fun `Context variable binding - carry on once`() {
        val expression = "library.loans@\$L"
        val LIBRARY = "library"
        val LOANS = "loans"
        val expected = mapper.createArrayNode()
        repeat(node[LIBRARY][LOANS].size()) {
            expected.add(node[LIBRARY])
        }
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `Context variable binding - carry on once and recall`() {
        val expression = "library.loans@\$L.\$L"
        val LIBRARY = "library"
        val LOANS = "loans"
        val expected = node[LIBRARY][LOANS]
        val actual= JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `Context variable binding - carry on twice`() {
        val expression = "library.loans@\$L.books@\$B"
        val LIBRARY = "library"
        val LOANS = "loans"
        val BOOKS = "books"
        val expected = mapper.createArrayNode()
        repeat(node[LIBRARY][LOANS].size()) {
            repeat(node[LIBRARY][BOOKS].size()) {
                expected.add(node[LIBRARY])
            }
        }
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `Context variable binding - carry on twice and recall`() {
        val expression = "library.loans@\$L.books@\$B.{\"title\": \$B.title}"
        val LIBRARY = "library"
        val LOANS = "loans"
        val BOOKS = "books"
        val TITLE = "title"
        val expected = mapper.createArrayNode()
        repeat(node[LIBRARY][LOANS].size()) {
            node[LIBRARY][BOOKS].forEach { book ->
                expected.addObject().set<JsonNode>("title", book[TITLE])
            }
        }
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#-context-variable-binding
     */
    @Test
    @Disabled
    fun `Context variable binding - join`() {
        val expression =
            "library.loans@\$L.books@\$B[\$L.isbn=\$B.isbn].{\"title\": \$B.title, \"customer\": \$L.customer}"

        @Language("JSON")
        val expected = mapper.readTree(
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

        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `Context variable binding - join composition `() {
        //val expression = "library.loans@\$L.books@\$B[\$L.isbn=\$B.isbn].customers[\$L.customer=id].{ 'customer': name, 'book': \$B.title, 'due': \$L.return }"
        //val expression = "library.loans@\$L.books@\$B[\$L.isbn=\$B.isbn].customers[\$L.customer=id]"
        val expression = "library.loans@\$L.books@\$B[\$L.isbn=\$B.isbn].customers[true]"

//        @Language("JSON")
//        val expected = mapper.readTree(
//            """
//            [
//              {
//                "customer": "Joe Doe",
//                "book": "Structure and Interpretation of Computer Programs",
//                "due": "2016-12-05"
//              },
//              {
//                 "customer": "Jason Arthur",
//                 "book": "Compilers: Principles, Techniques, and Tools",
//                 "due": "2016-10-22"
//               },
//               {
//                 "customer": "Jason Arthur",
//                 "book": "Structure and Interpretation of Computer Programs",
//                 "due": "2016-12-22"
//               }
//             ]
//            """.trimIndent()
//        )

        val actual = JSong(expression).evaluate(node)
        println(actual)
       //assertEquals(expected, actual)
    }


}


