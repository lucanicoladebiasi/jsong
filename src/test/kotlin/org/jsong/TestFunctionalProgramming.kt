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

}