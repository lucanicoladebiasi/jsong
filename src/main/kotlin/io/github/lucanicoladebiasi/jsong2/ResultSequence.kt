package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

class ResultSequence : List<JsonNode> {

    private val list = mutableListOf<JsonNode>()

    fun add(element: JsonNode?): ResultSequence {
        if (element != null) {
            when (element) {
                is ArrayNode -> list.addAll(element)
                else -> list.add(element)
            }
        }
        return this
    }

    fun select(fieldName: String): ResultSequence {
        val rs = ResultSequence()
        list.filter { node ->
            node is ObjectNode && node.has(fieldName)
        }.forEach { node ->
            rs.add(node[fieldName])
        }
        return rs
    }

    fun select(index: Int): ResultSequence {
        val rs = ResultSequence()
        val offset = if (index < 0) size + index else index
        if (offset in 0 until size) {
            rs.add(list[offset])
        }
        return rs
    }

    fun value(mapper: ObjectMapper = ObjectMapper()): JsonNode? {
        return when (size) {
            0 -> null
            1 -> list[0]
            else -> mapper.createArrayNode().addAll(list)
        }
    }

    override val size: Int
        get() = list.size

    override fun contains(element: JsonNode): Boolean {
        return list.contains(element)
    }

    override fun containsAll(elements: Collection<JsonNode>): Boolean {
        return list.containsAll(elements)
    }

    override fun get(index: Int): JsonNode {
        return list.get(index)
    }

    override fun indexOf(element: JsonNode): Int {
        return list.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    override fun iterator(): Iterator<JsonNode> {
        return list.iterator()
    }

    override fun lastIndexOf(element: JsonNode): Int {
        return list.lastIndexOf(element)
    }

    override fun listIterator(): ListIterator<JsonNode> {
        return list.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<JsonNode> {
        return list.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<JsonNode> {
        return list.subList(fromIndex, toIndex)
    }

} //~ Result