package org.jsonic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestFunctionalProgramming {

    /**
     * https://docs.jsonata.org/programming#variable-binding
     */
    @Test
    fun `variable binding`() {
        val expression = "Account.Order.Product.(\$p := Price; \$q := Quantity; \$p * \$q)"
        val expected = Processor().evaluate("[68.9, 21.67, 137.8, 107.99]")
        val actual = Processor(TestResources.invoice).evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/programming#defining-a-function
     */
    @Test
    fun `defining a function`() {
        val expression = "function(\$l, \$w, \$h){ \$k * \$w * \$h }"
        val expected = FunNode(listOf("l", "w", "h"), "\$k*\$w*\$h")
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/programming#defining-a-function
     */
    @Test
    fun `lambda function call`() {
        val expression = "function(\$l, \$w, \$h){ \$l * \$w * \$h }(10, 10, 5)"
        val expected = Processor().evaluate("500")
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/programming#defining-a-function
     */
    @Test
    fun `binding a function`() {
        val expression = "(\$volume := function(\$l, \$w, \$h){ \$l * \$w * \$h }; \$volume(10, 10, 5))"
        val expected = Processor().evaluate("500")
        val actual = Processor().evaluate(expression)
        assertEquals(expected, actual)
    }

}