package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestPathOperators {

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
        @Language("JSON")
        val expected = mapr.readTree(
            """
            ["order103","order103","order104","order104"]  
            """.trimIndent()
        )
        val actual = JSong.expression("Account.Order.Product.%.OrderID").evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators#-parent
     */
    @Test
    fun `Parent - 2`() {
        @Language("JSON")
        val expected = mapr.readTree(
            """
            ["Firefly", "Firefly", "Firefly", "Firefly"]
            """.trimIndent()
        )
        val actual = JSong.expression("Account.Order.Product.%.%.`Account Name`").evaluate(node)
        assertEquals(expected, actual)
    }

}