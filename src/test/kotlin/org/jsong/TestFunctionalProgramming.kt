package org.jsong

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestFunctionalProgramming {

    /**
     * https://docs.jsonata.org/programming#variable-binding
     */
    @Test
    fun `variable binding`() {
        val expression = "Account.Order.Product.(\$p := Price; \$q := Quantity; \$p * \$q)"
        val expected = JSong.of("[68.9, 21.67, 137.8, 107.99]").evaluate()
        val actual = JSong.of(expression).evaluate(TestResources.invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/programming#defining-a-function
     */
    @Test
    fun `defining a function`() {
        val expression = "function(\$l, \$w, \$h){ \$l * \$w * \$h }(10, 10, 5)"
        val expected = JSong.of("500").evaluate()
        val actual = JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/programming#defining-a-function
     */
    @Test
    fun `binding a function`() {
        val expression = "(\$volume := function(\$l, \$w, \$h){ \$l * \$w * \$h }; \$volume(10, 10, 5))"
        val expected = JSong.of("500").evaluate()
        val actual = JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

}