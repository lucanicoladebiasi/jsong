package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.TextNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestOtherOperators {

    private val mapper = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapper.readTree(Thread.currentThread().contextClassLoader.getResource("invoice.json"))
    }

    /**
     * https://docs.jsonata.org/other-operators#-concatenation
     */
    @Test
    fun `& (Concatenation)`() {
        val expression = "\"Hello\" & \"World\""
        val expected = TextNode("HelloWorld")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/other-operators#-chain
     */
    @Test
    @Disabled
    fun `Chain - single`() {
        val expression = "Account.Order.Product.(Price * Quantity) ~> \$sum()"
        val actual = JSong(expression).evaluate(node)
        val expected = DecimalNode(BigDecimal("336.36"))
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/other-operators#-chain
     */
    @Test
    @Disabled
    fun `Chain - multiple`() {
        @Language("JSON")
        val node = mapper.readTree("""
            {
              "Customer": {
                "Email": "freddy@my-social.com"
              }
            }
        """.trimIndent())
        val expression = "Customer.Email  ~> \$substringAfter(\"@\")~> \$substringBefore(\".\") ~> \$uppercase()"
        val expected = TextNode("MY-SOCIAL")
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `Chain - functional`() {
        val expression = "(\$uppertrim := \$trim ~> \$uppercase; \$uppertrim(\"   Hello    World   \"))"
        val expected = TextNode("HELLO WORLD")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/other-operators#--conditional
     */
    @Test
    @Disabled
    fun  `Conditional - positive`() {
        val expression = "45 < 50 ? \"Cheap\" : \"Expensive\""
        val expected = TextNode("Cheap")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/other-operators#--conditional
     */
    @Test
    @Disabled
    fun `Conditional - negative`() {
        val expression = "55 < 50 ? \"Cheap\" : \"Expensive\""
        val expected = TextNode("Expensive")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}