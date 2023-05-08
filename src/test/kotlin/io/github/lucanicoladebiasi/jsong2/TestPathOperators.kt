package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

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
        val actual = JSong.expression("Account.Order.Product.%.OrderID").evaluate(node)
        println(actual)
    }

    fun y() {
        JSong.expression("Account.Order.Product.%.%.`Account Name`")
    }

}