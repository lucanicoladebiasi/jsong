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
        val expected = mapr.readTree(
            """
                [
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
                  },
                  "Fred",
                  "Smith",
                  28,
                  {
                    "Street": "Hursley Park",
                    "City": "Winchester",
                    "Postcode": "SO21 2JN"
                  },
                  "Hursley Park",
                  "Winchester",
                  "SO21 2JN",
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
                  "077 7700 1234",
                  {
                    "type": "office",
                    "address": [
                      "fred.smith@my-work.com",
                      "fsmith@my-work.com"
                    ]
                  },
                  "office",
                  "fred.smith@my-work.com",
                  "fsmith@my-work.com",
                  {
                    "type": "home",
                    "address": [
                      "freddy@my-social.com",
                      "frederic.smith@very-serious.com"
                    ]
                  },
                  "home",
                  "freddy@my-social.com",
                  "frederic.smith@very-serious.com",
                  {
                    "Over 18 ?": true,
                    "Misc": null,
                    "Alternative.Address": {
                      "Street": "Brick Lane",
                      "City": "London",
                      "Postcode": "E1 6RF"
                    }
                  },
                  true,
                  null,
                  {
                    "Street": "Brick Lane",
                    "City": "London",
                    "Postcode": "E1 6RF"
                  },
                  "Brick Lane",
                  "London",
                  "E1 6RF"
                ]
            """.trimIndent()
        )

        val actual = JSong.expression(expression).evaluate(node)
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