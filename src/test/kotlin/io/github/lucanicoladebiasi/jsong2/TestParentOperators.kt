package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestParentOperators {

    private val mapr = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapr.readTree(Thread.currentThread().contextClassLoader.getResource("invoice.json"))
    }

    /**
     * https://docs.jsonata.org/path-operators#-parent
     */
    @Test
    fun `Parent - 1`() {
        val expression = "Account.Order.Product.%.OrderID"

        @Language("JSON")
        val expected = mapr.readTree(
            """
            ["order103","order103","order104","order104"]  
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#-parent
     */
    @Test
    fun `Parent - 2`() {
        val expression = "Account.Order.Product.%.%.`Account Name`"

        @Language("JSON")
        val expected = mapr.readTree(
            """
            ["Firefly", "Firefly", "Firefly", "Firefly"]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#-parent
     */
    @Test
    @Disabled
    fun `Parent - composite`() {
        val expression =
            """
            Account.Order.Product.{
               'Product': `Product Name`,
               'Order': %.OrderID,
               'Account': %.%.`Account Name`
            }
            """.trimIndent()

        @Language("JSON")
        val expected = mapr.readTree(
            """
            [
              {
                "Product": "Bowler Hat",
                "Order": "order103",
                "Account": "Firefly"
              },
              {
                "Product": "Trilby hat",
                "Order": "order103",
                "Account": "Firefly"
              },
              {
                "Product": "Bowler Hat",
                "Order": "order104",
                "Account": "Firefly"
              },
              {
                "Product": "Cloak",
                "Order": "order104",
                "Account": "Firefly"
              }
            ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

}