package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestGotoOperators {

    private val mapr = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapr.readTree(Thread.currentThread().contextClassLoader.getResource("ref.json"))
    }

    @Test
    fun `Select all 'ref'`() {
        val expression = "**.ref"

        @Language("JSON")
        val expected = mapr.readTree(
            """
              [
                "pulsar",
                "pulsar-output-topic",
                "resource-db",
                "resource-db",
                "resource-db",
                "resource-db",
                "resource-db",
                "resource-db"
              ]
            """.trimIndent()
        )
        val actual = JSong.expression(expression).evaluate(node)
        assertEquals(expected, actual)
    }

}