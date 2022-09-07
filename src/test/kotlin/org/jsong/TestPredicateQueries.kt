package org.jsong

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/predicate
 */
class TestPredicateQueries {

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
              "type": "office",
              "address": [
                "fred.smith@my-work.com",
                "fsmith@my-work.com"
              ]
            },
            {
              "type": "home",
              "address": [
                "freddy@my-social.com",
                "frederic.smith@very-serious.com"
              ]
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

    /**
     * https://docs.jsonata.org/predicate#predicates
     */
    @Test
    fun `Select the Phone items that have a type field that equals mobile`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            { "type": "mobile",  "number": "077 7700 1234" }
            """.trimIndent()
        )
        val actual = JSong.of("Phone|type='mobile'|").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#predicates
     */
//    @Test
//    fun `Select the mobile phone number`() {
//        val expression = "Phone[type='mobile'].number"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
//        @Language("JSON")
//        val expected = JSON.of("""
//            "077 7700 1234"
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/predicate#predicates
     */
//    @Test
//    fun `Select the office phone numbers - there are two of them!`() {
//        val expression = "Phone[type='office'].number"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
//        @Language("JSON")
//        val expected = JSON.of("""
//            [ "01962 001234",  "01962 001235" ]
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/predicate#singleton-array-and-value-equivalence
     */
//    @Test
//    fun `Singleton array and value equivalence - array of object`() {
//        val expression = "Address[].City"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
//        @Language("JSON")
//        val expected = JSON.of("""
//            ["Winchester"]
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/predicate#singleton-array-and-value-equivalence
     */
//    @Test
//    @Disabled
//    fun `Singleton array and value equivalence - array of element`() {
//        val expression = "Phone[0][].number"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
//        @Language("JSON")
//        val expected = JSON.of("""
//            [ "0203 544 1234" ]
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/predicate#singleton-array-and-value-equivalence
     */
//    @Test
//    fun `Singleton array and value equivalence - array then predicate`() {
//        val expression = "Phone[][type='home'].number"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
//        @Language("JSON")
//        val expected = JSON.of("""
//             [ "0203 544 1234" ]
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/predicate#singleton-array-and-value-equivalence
     */
//    @Test
//    fun `Singleton array and value equivalence - predicate then array`() {
//        val expression = "Phone[type='office'].number[]"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
//        @Language("JSON")
//        val expected = JSON.of("""
//            [ "01962 001234", "01962 001235" ]
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/predicate#wildcards
     */
//    @Test
//    fun `Use of asterix instead of field name to select all fields in an object - postfix`() {
//        val expression = "Address.*"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
//        @Language("JSON")
//        val expected = JSON.of("""
//            [ "Hursley Park", "Winchester", "SO21 2JN" ]
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/predicate#wildcards
     */
//    @Test
//    fun `Use of asterix instead of field name to select all fields in an object - prefix`() {
//        val expression = "*.Postcode"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
//        @Language("JSON")
//        val expected = JSON.of("""
//            "SO21 2JN"
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

    /**
     * https://docs.jsonata.org/predicate#navigate-arbitrary-depths
     */
//    @Test
//    fun `Navigate arbitrary depths`() {
//        val expression = "**.Postcode"
//        val evaluation = JSonata.of(expression).evaluate(JSON.of(doc))
//        @Language("JSON")
//        val expected = JSON.of("""
//            [ "SO21 2JN", "E1 6RF" ]
//        """.trimIndent())
//        assertEquals(expected, evaluation.context)
//    }

}