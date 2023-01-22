package org.jsonic

import org.intellij.lang.annotations.Language
import org.jsong._TestResources
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * https://docs.JSong.org/predicate
 */
class TestPredicateQueries {

    /**
     * https://docs.JSong.org/predicate#predicates
     */
    @Test
    fun `Select the Phone items that have a type field that equals mobile`() {
        val expression = "Phone[type='mobile']"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
                { "type": "mobile",  "number": "077 7700 1234" }
                """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.JSong.org/predicate#predicates
     */
    @Test
    fun `Select the mobile phone number`() {
        val expression = "Phone[type='mobile'].number"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
                "077 7700 1234"
                """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.JSong.org/predicate#predicates
     */
    @Test
    fun `Select the office phone numbers - there are two of them!`() {
        val expression = "Phone[type='office'].number"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
            [ "01962 001234",  "01962 001235" ]
            """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.JSong.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    fun `Singleton array and value equivalence - array of object`() {
        val expression = "Address[].City"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
            ["Winchester"]
            """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.JSong.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    fun `Singleton array and value equivalence - array of element`() {
        val expression = "Phone[0][].number"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
            [ "0203 544 1234" ]
            """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.JSong.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    fun `Singleton array and value equivalence - array then predicate`() {
        val expression = "Phone[][type='home'].number"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
             [ "0203 544 1234" ]
            """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.JSong.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    fun `Singleton array and value equivalence - predicate then array`() {
        val expression = "Phone[type='office'].number[]"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
            [ "01962 001234", "01962 001235" ]
            """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.JSong.org/predicate#wildcards
     */
    @Test
    fun `Use of asterix instead of field name to select all fields in an object - postfix`() {
        val expression = "Address.*"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
            [ "Hursley Park", "Winchester", "SO21 2JN" ]
            """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.JSong.org/predicate#wildcards
     */
    @Test
    fun `Use of asterix instead of field name to select all fields in an object - prefix`() {
        val expresssion = "*.Postcode"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
            "SO21 2JN"
            """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expresssion)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.JSong.org/predicate#navigate-arbitrary-depths
     */
    @Test
    fun `Navigate arbitrary depths`() {
        val expression = "**.Postcode"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree(
            """
            [ "SO21 2JN", "E1 6RF" ]
            """.trimIndent()
        )
        val actual = Processor(_TestResources.address).evaluate(expression)
        assertEquals(expected, actual)
    }

}