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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/predicate
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestPredicateQueries {

    private val mapper = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapper.readTree(Thread.currentThread().contextClassLoader.getResource("address.json"))
    }

    /**
     * https://docs.jsonata.org/predicate#predicates
     */
    @Test
    fun `Select the Phone items that have a type field that equals mobile`() {
        val expression = "Phone[type='mobile']"

        @Language("JSON")
        val expected = mapper.readTree(
            """
                { "type": "mobile",  "number": "077 7700 1234" }
                """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#predicates
     */
    @Test
    fun `Select the mobile phone number`() {
        val expression = "Phone[type='mobile'].number"

        @Language("JSON")
        val expected = mapper.readTree(
            """
                "077 7700 1234"
                """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#predicates
     */
    @Test
    fun `Select the office phone numbers - there are two of them!`() {
        val expression = "Phone[type='office'].number"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            [ "01962 001234",  "01962 001235" ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    fun `Singleton array and value equivalence - array of object`() {
        val expression = "Address[].City"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            ["Winchester"]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    fun `Singleton array and value equivalence - array of element`() {
        val expression = "Phone[0][].number"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            [ "0203 544 1234" ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    fun `Singleton array and value equivalence - array then predicate`() {
        val expression = "Phone[][type='home'].number"

        @Language("JSON")
        val expected = mapper.readTree(
            """
             [ "0203 544 1234" ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    fun `Singleton array and value equivalence - predicate then array`() {
        val expression = "Phone[type='office'].number[]"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            [ "01962 001234", "01962 001235" ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#wildcards
     */
    @Test
    fun `Use of asterix instead of field name to select all fields in an object - postfix`() {
        val expression = "Address.*"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            [ "Hursley Park", "Winchester", "SO21 2JN" ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#wildcards
     */
    @Test
    fun `Use of asterix instead of field name to select all fields in an object - prefix`() {
        val expression = "*.Postcode"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            "SO21 2JN"
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#navigate-arbitrary-depths
     */
    @Test
    fun `Navigate arbitrary depths`() {
        val expression = "**.Postcode"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            [ "SO21 2JN", "E1 6RF" ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

}