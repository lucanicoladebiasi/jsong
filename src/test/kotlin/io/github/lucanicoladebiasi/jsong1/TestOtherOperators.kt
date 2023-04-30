package io.github.lucanicoladebiasi.jsong1

import com.fasterxml.jackson.databind.node.TextNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestOtherOperators {

    /**
     * https://docs.jsonata.org/other-operators#-concatenation
     */
    @Test
    fun `& (Concatenation)`() {
        val expression = "\"Hello\" & \"World\""
        val expected = TextNode("HelloWorld")
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/other-operators#-chain
     */
    @Test
    fun `Chain - single`() {
        val expression = "Account.Order.Product.(Price * Quantity) ~> \$sum()"
        val actual = Processor(TestResources1.invoice).evaluate(expression)
        val expected = Processor(TestResources1.invoice).evaluate("\$sum(Account.Order.Product.(Price * Quantity))")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/other-operators#-chain
     */
    @Test
    fun `Chain - multiple`() {
        @Language("JSON")
        val json = TestResources1.mapper.readTree("""
            {
              "Customer": {
                "Email": "freddy@my-social.com"
              }
            }
        """.trimIndent())
        val expression = "Customer.Email  ~> \$substringAfter(\"@\")~> \$substringBefore(\".\") ~> \$uppercase()"
        val expected = Processor(json).evaluate("\$uppercase(\$substringBefore(\$substringAfter(Customer.Email, \"@\"), \".\"))")
        val actual = Processor(json).evaluate(expression)
        assertEquals(expected, actual)
    }

    @Test
    fun `Chain - functional`() {
        val expression = "(\$uppertrim := \$trim ~> \$uppercase; \$uppertrim(\"   Hello    World   \"))"
        val expected = TextNode("HELLO WORLD")
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/other-operators#--conditional
     */
    @Test
    fun  `Conditional - positive`() {
        val expression = "45 < 50 ? \"Cheap\" : \"Expensive\""
        val expected = TextNode("Cheap")
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/other-operators#--conditional
     */
    @Test
    fun `Conditional - negative`() {
        val expression = "55 < 50 ? \"Cheap\" : \"Expensive\""
        val expected = TextNode("Expensive")
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

}