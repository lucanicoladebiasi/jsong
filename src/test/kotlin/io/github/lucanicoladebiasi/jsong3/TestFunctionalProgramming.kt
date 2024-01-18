/**
 * MIT License
 *
 * Copyright (c) 2023 Luca Nicola Debiasi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.DecimalNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

/**
 * https://docs.jsonata.org/programming
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestFunctionalProgramming {

    private val om = ObjectMapper()

    private lateinit var node: JsonNode

    @BeforeAll
    fun setUp() {
        node = om.readTree(Thread.currentThread().contextClassLoader.getResource("invoice.json"))
    }

    /**
     * https://docs.jsonata.org/programming#variable-binding
     */
    @Test
    fun `variable binding`() {
        val expression = "Account.Order.Product.(\$p := Price; \$q := Quantity; \$p * \$q)"

        @Language("JSON")
        val expected = om.createArrayNode().addAll(
            om.readTree(
                """
            [68.9, 21.67, 137.8, 107.99]
            """.trimIndent()
            ).map {
                DecimalNode(it.decimalValue())
            }
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/programming#defining-a-function
     */
    @Test
    fun `defining a function`() {
        val expression = "function(\$l, \$w, \$h){ \$k * \$w * \$h }"
        val expected = FunctionNode(listOf("l", "w", "h"), "\$k*\$w*\$h", om)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/programming#defining-a-function
     */
    @Test
    fun `lambda function call`() {
        val expression = "function(\$l, \$w, \$h){ \$l * \$w * \$h }(10, 10, 5)"
        val expected = DecimalNode(BigDecimal("500"))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)

    }

    /**
     * https://docs.jsonata.org/programming#defining-a-function
     */
    @Test
    fun `binding a function`() {
        val expression = "(\$volume := function(\$l, \$w, \$h){ \$l * \$w * \$h }; \$volume(10, 10, 5))"
        val expected = DecimalNode(BigDecimal("500"))
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}