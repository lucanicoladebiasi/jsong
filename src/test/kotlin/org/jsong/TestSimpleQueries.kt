package org.jsong

import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Implement the examples described at [https://docs.jsonata.org/simple](https://docs.jsonata.org/simple).
 */
class TestSimpleQueries {

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Returns a JSON string`() {
        val expected = TextNode("Smith")
        val actual = JSong.of("Surname").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Returns a JSON number`() {
        val expected = 28
        val actual = JSong.of("Age").evaluate(TestResources.address)
        assertEquals(expected, actual?.asInt())
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Field references are separated by dot`() {
        val expected = TextNode("Winchester")
        val actual = JSong.of("Address.City").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Matched the path and returns the null value`() {
        val expected = NullNode.instance
        val actual = JSong.of("Other.Misc").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Path not found`() {
        val actual = JSong.of("Other.Nothing").evaluate(TestResources.address)
        assertNull(actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Field references containing whitespace or reserved tokens can be enclosed in backticks`() {
        val expected = BooleanNode.TRUE
        val actual = JSong.of("Other.`Over 18 ?`").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Returns the first item`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            { 
              "type": "home", 
              "number": "0203 544 1234"
            }
        """.trimIndent()
        )
        val actual = JSong.of("Phone[0]").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Returns the second item`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            { 
              "type": "office", 
              "number": "01962 001234" }
        """.trimIndent()
        )
        val actual = JSong.of("Phone|1|").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Return the last item`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            { 
              "type": "mobile", 
              "number": "077 7700 1234"
            }
        """.trimIndent()
        )
        val actual = JSong.of("Phone|-1|").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Negative indexed count from the end`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            { 
              "type": "office", 
              "number": "01962 001235"
            }
        """.trimIndent()
        )
        val actual = JSong.of( "Phone|-2|").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Doesn't exist - returns nothing`() {
        val actual = JSong.of("Phone|8|").evaluate(TestResources.address)
        assertNull(actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Selects the number field in the first item`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            "0203 544 1234"
        """.trimIndent()
        )
        val actual = JSong.of("Phone|0|.number").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `No index is given to Phone so it selects all of them (the whole array), then it selects all the number fields for each of them`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [ 
              "0203 544 1234", 
              "01962 001234", 
              "01962 001235", 
              "077 7700 1234" 
            ]
            """.trimIndent()
        )
        val actual = JSong.of("Phone.number").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Might expect it to just return the first number, but it returns the first number of each of the items selected by Phone`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [ 
              "0203 544 1234", 
              "01962 001234", 
              "01962 001235", 
              "077 7700 1234" 
            ]
            """.trimIndent()
        )
        val actual = JSong.of("Phone.number|0|").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Applies the index to the array returned by Phone dot number`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            "0203 544 1234"
            """.trimIndent()
        )
        val actual = JSong.of("(Phone.number)|0|").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Returns a range of items by creating an array of indexes`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [
              { "type": "home", "number": "0203 544 1234" },
              { "type": "office", "number": "01962 001234" }
            ]
        """.trimIndent()
        )
        val actual = JSong.of("Phone|[0..1]|").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
//    @Test
//    fun `$ at the start of an expression refers to the entire input document`() {
//        val expression = "$[0]"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(array))
//        @Language("JSON")
//        val expected = JSON.of("""
//            { "ref": [ 1,2 ] }
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
//    @Test
//    fun `Dot ref here returns the entire internal array`() {
//        val expression = "$[0].ref"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(array))
//        val expected = JSON.of("[1, 2]")
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
//    @Test
//    fun `Returns element on first position of the internal array`() {
//        val expression = "$[0].ref[0]"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(array))
//        val expected = JSON.of("1")
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
//    @Test
//    fun `Despite the structure of the nested array, the resultant selection is flattened into a single flat array`() {
//        val expression = "$.ref"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(array))
//        val expected = JSON.of("[1, 2, 3, 4]")
//        assertEquals(expected, evaluation.context)
//    }

}