package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SequenceNodeTest {

    private val mapr = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapr.readTree(Thread.currentThread().contextClassLoader.getResource("address.json"))
    }

    @Test
    fun `of ObjectNode`() {
        val seq = SequenceNode(mapr.nodeFactory).append(node)
        assertEquals(node, seq.value)
    }
}