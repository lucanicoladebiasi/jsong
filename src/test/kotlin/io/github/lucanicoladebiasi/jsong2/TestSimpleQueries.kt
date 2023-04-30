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
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Implement the examples described at [https://docs.jsonata.org/simple](https://docs.jsonata.org/simple).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSimpleQueries {

    private val mapr = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapr.readTree(Thread.currentThread().contextClassLoader.getResource("address.json"))
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Returns a JSON string`() {
        val expression = "Surname"
        val expected = TextNode("Smith")
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Returns a JSON number`() {
        val expression = "Age"
        val expected = IntNode(28)
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata\.org/simple#navigating-json-objects
     */
    @Test
    fun `Field references are separated by dot`() {
        val expression = "Address.City"
        val expected = TextNode("Winchester")
        val actual =JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Matched the path and returns the null value`() {
        val expression = "Other.Misc"
        val expected = NullNode.instance
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Path not found`() {
        val expression = "Other.Nothing"
        val actual = JSong.expression(expression).evaluate(node)
        assertNull(actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Field references containing whitespace or reserved tokens can be enclosed in backticks`() {
        val expression = "Other.`Over 18 ?`"
        val expected = BooleanNode.TRUE
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Returns the first item`() {
        val expression = "Phone[0]"
        @Language("JSON")
        val expected = mapr.readTree(
            """
            { 
              "type": "home", 
              "number": "0203 544 1234"
            }
        """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Returns the second item`() {
        val expression = "Phone[1]"
        @Language("JSON")
        val expected = mapr.readTree(
            """
            { 
              "type": "office", 
              "number": "01962 001234" }
        """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Return the last item`() {
        val expression = "Phone[-1]"
        @Language("JSON")
        val expected = mapr.readTree(
            """
            { 
              "type": "mobile", 
              "number": "077 7700 1234"
            }
        """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Negative indexed count from the end`() {
        val expression = "Phone[-2]"
        @Language("JSON")
        val expected = mapr.readTree(
            """
            {
              "type": "office",
              "number": "01962 001235"
            }
        """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Doesn't exist - returns nothing`() {
        val expression = "Phone[8]"
        val actual = JSong.expression(expression).evaluate(node)
        assertNull(actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Selects the number field in the first item`() {
        val expression = "Phone[0].number"
        @Language("JSON")
        val expected = mapr.readTree(
            """
            "0203 544 1234"
        """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
 }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `No index is given to Phone so it selects all of them (the whole array), then it selects all the number fields for each of them`() {
        val expression = "Phone.number"
        @Language("JSON")
        val expected = mapr.readTree(
            """
            [
              "0203 544 1234",
              "01962 001234",
              "01962 001235",
              "077 7700 1234"
            ]
            """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Might expect it to just return the first number, but it returns the first number of each of the items selected by Phone`() {
        val expression = "Phone.number[0]"
        @Language("JSON")
        val expected = mapr.readTree(
            """
            [
              "0203 544 1234",
              "01962 001234",
              "01962 001235",
              "077 7700 1234"
            ]
            """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
//    @Test
//    fun `Applies the index to the array returned by Phone dot number`() {
//        val expression = "(Phone.number)[0]"
//        @Language("JSON")
//        val expected = TestResources.mapper.readTree(
//            """
//            "0203 544 1234"
//            """.trimIndent()
//        )
//        val actual = JSong.expression(expression).evaluate(TestResources.address)
//        assertEquals(expected, actual)
//    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
//    @Test
//    fun `Returns a range of items by creating an array of indexes`() {
//        val expression = "Phone[[0..1]]"
//        @Language("JSON")
//        val expected = TestResources.mapper.readTree(
//            """
//            [
//              { "type": "home", "number": "0203 544 1234" },
//              { "type": "office", "number": "01962 001234" }
//            ]
//        """.trimIndent()
//        )
//        val actual = JSong.expression(expression).evaluate(TestResources.address)
//        assertEquals(expected, actual)
//    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
//    @Test
//    fun `$ at the start of an expression refers to the entire input document`() {
//        val expression = "$[0]"
//        @Language("JSON")
//        val expected = TestResources.mapper.readTree("""
//            { "ref": [ 1,2 ] }
//        """.trimIndent())
//        val actual = JSong.expression(expression).evaluate(TestResources.array)
//        assertEquals(expected, actual)
//    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
//    @Test
//    fun `Dot ref here returns the entire internal array`() {
//        val expression = "$[0].ref"
//        val expected = TestResources.mapper.readTree("[1, 2]")
//        val actual = JSong.expression(expression).evaluate(TestResources.address)
//        assertEquals(expected, actual)
//    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
//    @Test
//    fun `Returns element on first position of the internal array`() {
//        val expression = "$[0].ref[0]"
//        val expected = TestResources.mapper.readTree("1")
//        val actual = JSong.expression(expression).evaluate(TestResources.address)
//        assertEquals(expected, actual)
//    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
//    @Test
//    fun `Despite the structure of the nested array, the resultant selection is flattened into a single flat array`() {
//        val expression = "$.ref"
//        val expected = TestResources.mapper.readTree("[1, 2, 3, 4]")
//        val actual = JSong.expression(expression).evaluate(TestResources.address)
//        assertEquals(expected, actual)
//    }

}