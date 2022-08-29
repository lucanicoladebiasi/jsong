package com.tradeix.jsonata

import org.intellij.lang.annotations.Language
import com.tradeix.jsonata.json.JSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Implement the examples described at [https://docs.jsonata.org/simple](https://docs.jsonata.org/simple).
 */
class JSonataTestSimpleQueries {

    @Language("JSON")
    val doc = """
        {
          "FirstName": "Fred",
          "Surname": "Smith",
          "Age": 28,
          "Address": {
            "Street": "Hursley Park",
            "City": "Winchester",
            "Postcode": "SO21 2JN"
          },
          "Phone": [
            {
              "type": "home",
              "number": "0203 544 1234"
            },
            {
              "type": "office",
              "number": "01962 001234"
            },
            {
              "type": "office",
              "number": "01962 001235"
            },
            {
              "type": "mobile",
              "number": "077 7700 1234"
            }
          ],
          "Email": [
            {
              "type": "work",
              "address": ["fred.smith@my-work.com", "fsmith@my-work.com"]
            },
            {
              "type": "home",
              "address": ["freddy@my-social.com", "frederic.smith@very-serious.com"]
            }
          ],
          "Other": {
            "Over 18 ?": true,
            "Misc": null,
            "Alternative.Address": {
              "Street": "Brick Lane",
              "City": "London",
              "Postcode": "E1 6RF"
            }
          }
        }
    """.trimIndent()

    @Language("JSON")
    val array = """
        [
          { "ref": [ 1,2 ] },
          { "ref": [ 3,4 ] }
        ]
    """.trimIndent()

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Returns a JSON string`() {
        val expression = "Surname"
        val expected = JSON.of("\"Smith\"")
        val actual = JSonata.of(expression).evaluate(JSON.of(doc)).context
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Returns a JSON number`() {
        val expression = "Age"
        val expected = JSON.of("28")
        val actual = JSonata.of(expression).evaluate(JSON.of(doc)).context
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Field references are separated by dot`() {
        val expression = "Address.City"
        val expected = JSON.of("\"Winchester\"")
        val actual = JSonata.of(expression).evaluate(JSON.of(doc)).context
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Matched the path and returns the null value`() {
        val expression = "Other.Misc"
        val expected = JSON.of("null")
        val actual = JSonata.of(expression).evaluate(JSON.of(doc)).context
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Path not found`() {
        val expression = "Other.Nothing"
        val actual = JSonata.of(expression).evaluate(JSON.of(doc)).context
        assertNull(actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-objects
     */
    @Test
    fun `Field references containing whitespace or reserved tokens can be enclosed in backticks`() {
        val expression = "Other.`Over 18 ?`"
        val expected = JSON.of("true")
        val actual = JSonata.of(expression).evaluate(JSON.of(doc)).context
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Returns the first item`() {
        val expression = "Phone[0]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))

        @Language("JSON")
        val expected = JSON.of(
            """
            { "type": "home", "number": "0203 544 1234" }
        """.trimIndent()
        )
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Returns the second item`() {
        val expression = "Phone[1]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))

        @Language("JSON")
        val expected = JSON.of(
            """
            { "type": "office", "number": "01962 001234" }
        """.trimIndent()
        )
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Return the last item`() {
        val expression = "Phone[-1]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))

        @Language("JSON")
        val expected = JSON.of(
            """
            { "type": "mobile", "number": "077 7700 1234" }
        """.trimIndent()
        )
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Negative indexed count from the end`() {
        val expression = "Phone[-2]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))

        @Language("JSON")
        val expected = JSON.of(
            """
            { "type": "office", "number": "01962 001235" }
        """.trimIndent()
        )
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Doesn't exist - returns nothing`() {
        val expression = "Phone[8]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
        assertNull(evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Selects the number field in the first item`() {
        val expression = "Phone[0].number"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))

        @Language("JSON")
        val expected = JSON.of(
            """
            "0203 544 1234"
        """.trimIndent()
        )
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `No index is given to Phone so it selects all of them (the whole array), then it selects all the number fields for each of them`() {
        val expression = "Phone.number"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))

        @Language("JSON")
        val expected = JSON.of(
            """
            [ "0203 544 1234", "01962 001234", "01962 001235", "077 7700 1234" ]
        """.trimIndent()
        )
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Might expect it to just return the first number, but it returns the first number of each of the items selected by Phone`() {
        val expression = "Phone.number[0]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))

        @Language("JSON")
        val expected = JSON.of(
            """
            [ "0203 544 1234", "01962 001234", "01962 001235", "077 7700 1234" ]
        """.trimIndent()
        )
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Applies the index to the array returned by Phone dot number`() {
        val expression = "(Phone.number)[0]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
        @Language("JSON")
        val expected = JSON.of(
            """
            "0203 544 1234"
        """.trimIndent()
        )
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#navigating-json-arrays
     */
    @Test
    fun `Returns a range of items by creating an array of indexes`() {
        val expression = "Phone[[0..1]]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))

        @Language("JSON")
        val expected = JSON.of(
            """
            [
              { "type": "home", "number": "0203 544 1234" },
              { "type": "office", "number": "01962 001234" }
            ]
        """.trimIndent()
        )
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
    @Test
    fun `$ at the start of an expression refers to the entire input document`() {
        val expression = "$[0]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(array))
        @Language("JSON")
        val expected = JSON.of("""
            { "ref": [ 1,2 ] }
        """.trimIndent())
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
    @Test
    fun `Dot ref here returns the entire internal array`() {
        val expression = "$[0].ref"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(array))
        val expected = JSON.of("[1, 2]")
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
    @Test
    fun `Returns element on first position of the internal array`() {
        val expression = "$[0].ref[0]"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(array))
        val expected = JSON.of("1")
        assertEquals(expected, evaluation.context)
    }

    /**
     * https://docs.jsonata.org/simple#top-level-arrays-nested-arrays-and-array-flattening
     */
    @Test
    fun `Despite the structure of the nested array, the resultant selection is flattened into a single flat array`() {
        val expression = "$.ref"
        val evaluation = JSonata.of(expression).evaluate(JSON.of(array))
        val expected = JSON.of("[1, 2, 3, 4]")
        assertEquals(expected, evaluation.context)
    }

}