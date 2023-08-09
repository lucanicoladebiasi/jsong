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
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
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
    fun `Positional variable binding`() {
        val expression = "library.books#\$i[\"Kernighan\" in authors].{\"title\": title, \"index\": \$i }"

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
    fun `Context variable binding - carry on once`() {
        val LIBRARY = "library"
        val LOANS = "loans"
        val library = node[LIBRARY] as ObjectNode
        val loans = node[LIBRARY][LOANS] as ArrayNode
        val expression = "library.loans@\$L"
        val expected = mapper.createArrayNode()
        repeat(loans.size()) {
            expected.add(library)
        }
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    fun `Context variable binding - carry on once and recall`() {
        val LIBRARY = "library"
        val LOANS = "loans"
        val expected = node[LIBRARY][LOANS]
        val expression = "library.loans@\$L.\$L"
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `Context variable binding - carry on twice`() {
        val expression = "library.loans@\$L.books@\$B"
        val actual = JSong(expression).evaluate(node)
//        val expected = mapper.createArrayNode().let {
//            for (i in 1..loans.size() * books.size()) {
//                it.add(library)
//            }
//            it
//        }
//        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `Context variable binding - carry on twice and recall`() {
//        val expression = "library.loans@\$L.books@\$B.{\"title\": \$B.title}"
//        val actual = JSong(expression).evaluate(node)
//        val loans = JSong("library.loans").evaluate(node)
//        val expected = mapper.createArrayNode().let {
//            for (i in 1..loans!!.size()) {
//                titles.forEach { title ->
//                    it.addObject().set<JsonNode>("title", title)
//                }
//            }
//            it
//        }
//        assertEquals(expected, actual)
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

}


