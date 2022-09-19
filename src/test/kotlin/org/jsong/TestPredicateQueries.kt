package org.jsong

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Disabled
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
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
                { "type": "mobile",  "number": "077 7700 1234" }
                """.trimIndent()
        )
        val actual = JSong.of("Phone[type='mobile']").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
    /**
     * https://docs.JSong.org/predicate#predicates
     */
    @Test
    fun `Select the mobile phone number`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
                "077 7700 1234"
                """.trimIndent()
        )
        val actual = JSong.of("Phone[type='mobile'].number").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
    /**
     * https://docs.JSong.org/predicate#predicates
     */
    @Test
    fun `Select the office phone numbers - there are two of them!`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [ "01962 001234",  "01962 001235" ]
            """.trimIndent()
        )
        val actual = JSong.of("Phone[type='office'].number").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
    /**
     * https://docs.JSong.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    @Disabled
    fun `Singleton array and value equivalence - array of object`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            ["Winchester"]
            """.trimIndent()
        )
        val actual = JSong.of("Address[].City").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
    /**
     * https://docs.JSong.org/predicate#singleton-array-and-value-equivalence
     */
    @Test
    @Disabled
    fun `Singleton array and value equivalence - array of element`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [ "0203 544 1234" ]
            """.trimIndent()
        )
        val actual = JSong.of("Phone[0][].number").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
    /**
     * https://docs.JSong.org/predicate#singleton-array-and-value-equivalence
     */
    @Disabled
    @Test
    fun `Singleton array and value equivalence - array then predicate`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
             [ "0203 544 1234" ]
            """.trimIndent()
        )
        val actual = JSong.of("Phone[][type='home'].number").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
    /**
     * https://docs.JSong.org/predicate#singleton-array-and-value-equivalence
     */
    @Disabled
    @Test
    fun `Singleton array and value equivalence - predicate then array`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [ "01962 001234", "01962 001235" ]
            """.trimIndent()
        )
        val actual = JSong.of("Phone[type='office'].number[]").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
    /**
     * https://docs.JSong.org/predicate#wildcards
     */
    @Disabled
    @Test
    fun `Use of asterix instead of field name to select all fields in an object - postfix`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [ "Hursley Park", "Winchester", "SO21 2JN" ]
            """.trimIndent()
        )
        val actual = JSong.of("Address.*").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
    /**
     * https://docs.JSong.org/predicate#wildcards
     */
    @Disabled
    @Test
    fun `Use of asterix instead of field name to select all fields in an object - prefix`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            "SO21 2JN"
        """.trimIndent()
        )
        val actual = JSong.of("*.Postcode").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
    /**
     * https://docs.JSong.org/predicate#navigate-arbitrary-depths
     */
    @Disabled
    @Test
    fun `Navigate arbitrary depths`() {
        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [ "SO21 2JN", "E1 6RF" ]
        """.trimIndent()
        )
        val actual = JSong.of("**.Postcode").evaluate(TestResources.address)
        assertEquals(expected, actual)
    }
}