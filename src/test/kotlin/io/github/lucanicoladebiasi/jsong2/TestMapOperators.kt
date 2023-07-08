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
package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/path-operators
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestMapOperators {

    private val mapper = ObjectMapper()

    private var address: JsonNode? = null

    private var invoice: JsonNode? = null

    @BeforeAll
    fun setUp() {
        address = mapper.readTree(Thread.currentThread().contextClassLoader.getResource("address.json"))
        invoice = mapper.readTree(Thread.currentThread().contextClassLoader.getResource("invoice.json"))
    }

    /**
     * https://docs.jsonata.org/path-operators
     */
    @Test
    fun `map results in scalar`() {
        val expression = "Address.City"
        val expected = TextNode("Winchester")
        val actual = JSong(expression).evaluate(address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators
     */
    @Test
    fun `map results in array`() {
        val expression = "Phone.number"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            [ "0203 544 1234", "01962 001234", "01962 001235", "077 7700 1234" ]                
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators
     */
    @Test
    fun `map multiplication`() {
        val expression = "Account.Order.Product.(Price * Quantity)"

        @Language("JSON")
        val expected = mapper.createArrayNode().addAll(mapper.readTree(
            """
            [ 68.9, 21.67, 137.8, 107.99 ]
            """.trimIndent()
        ).filterIsInstance<NumericNode>().map { DecimalNode(it.decimalValue()) })
        val actual = JSong(expression).evaluate(invoice)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/path-operators
     */
    @Test
    @Disabled
    fun `map function`() {
        val expression = "Account.Order.OrderID.\$uppercase()"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            [ "ORDER103", "ORDER104" ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(invoice)
        assertEquals(expected, actual)
    }

}


