package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestInstance
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestWildcardOperators {

    private val mapr = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapr.readTree(Thread.currentThread().contextClassLoader.getResource("address.json"))
    }

    @Test
    fun `Wildcard context - prefix`() {
        val expression = "*.Postcode"

        @Language("JSON")
        val expected = mapr.readTree(
            """
                "SO21 2JN"
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    fun `Wildcard context - postfix`() {
        val expression = "Address.*"

        @Language("JSON")
        val expected = mapr.readTree(
            """
                [
                  "Hursley Park",
                  "Winchester",
                  "SO21 2JN"
                ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `Wildcard descendants - prefix`() {
        val expression = "**.Postcode"

        @Language("JSON")
        val expected = mapr.readTree(
            """
                [
                  "SO21 2JN",
                  "E1 6RF"
                ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `Wildcard descendants - postfix`() {
        val expression = "Address.**"

        @Language("JSON")
        val expected = mapr.readTree(
            """
                [
                  {
                    "Street": "Hursley Park",
                    "City": "Winchester",
                    "Postcode": "SO21 2JN"
                  },
                  "Hursley Park",
                  "Winchester",
                  "SO21 2JN"
                ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

}