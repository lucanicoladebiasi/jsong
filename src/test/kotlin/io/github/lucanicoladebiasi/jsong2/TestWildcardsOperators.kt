package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestWildcardsOperators {

    private val mapr = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapr.readTree(Thread.currentThread().contextClassLoader.getResource("address.json"))
    }

    /**
     * https://docs.jsonata.org/predicate#wildcards
     */
    @Test
    fun `Wildcard - select the values of all the fields`() {
        val expression = "Address.*"

        @Language("JSON")
        val expected = mapr.readTree(
            """
            [ "Hursley Park", "Winchester", "SO21 2JN" ]
            """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#wildcards
     */
    @Test
    fun `Wildcard - select the values of any child object`() {
        val expression = "*.Postcode"

        @Language("JSON")
        val expected = mapr.readTree(
            """
            "SO21 2JN"
            """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    fun `Wildcard - select the values of an array`() {
        val expression = "Phone.*"

        @Language("JSON")
        val expected = mapr.readTree(
            """
            [
              "home",
              "0203 544 1234",
              "office",
              "01962 001234",
              "office",
              "01962 001235",
              "mobile",
              "077 7700 1234"
            ]
            """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/predicate#navigate-arbitrary-depths
     */
    @Test
    fun `Descendants - prefix`() {
        val expression = "**.Postcode"

        @Language("JSON")
        val expected = mapr.readTree(
            """
            [ "SO21 2JN", "E1 6RF" ]
            """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    fun `Descendants - postfix`() {
        val expression = "Phone.**"

        @Language("JSON")
        val expected = mapr.readTree(
            """
            [
              {
                "type": "home",
                "number": "0203 544 1234"
              },
              "home",
              "0203 544 1234",
              {
                "type": "office",
                "number": "01962 001234"
              },
              "office",
              "01962 001234",
              {
                "type": "office",
                "number": "01962 001235"
              },
              "office",
              "01962 001235",
              {
                "type": "mobile",
                "number": "077 7700 1234"
              },
              "mobile",
              "077 7700 1234"
            ]
            """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    fun `Descendants - alone`() {
        val expression = "**"
        @Language("JSON")
        val node = mapr.readTree(
            """
                {
                  "schema": {
                    "version": "1.0",
                    "type": "service"
                  }
                }
            """.trimIndent()
        )
        @Language("JSON")
        val expected = mapr.readTree(
            """
            [
              {
                "schema": {
                  "version": "1.0",
                  "type": "service"
                }
              },
              {
                "version": "1.0",
                "type": "service"
              },
              "1.0",
              "service"
            ]
        """.trimIndent()
        )
        val actual = desc(node)
        println(actual)
        assertEquals(expected, actual)
    }

    fun desc(node: JsonNode): ArrayNode {
        val res = SequenceNode(mapr.nodeFactory)
        when(node) {
            is ArrayNode -> {
                res.add(node)
                node.forEach {
                    res.addAll(desc(it))
                }
            }
            is ObjectNode -> {
                res.add(node)
                node.fields().forEach {
                    res.addAll(desc(it.value))
                }
            }
            else -> {
                res.add(node)
            }
        }
        return res
    }

}