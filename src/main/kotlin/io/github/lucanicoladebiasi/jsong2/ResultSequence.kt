package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

class ResultSequence : List<Context> {

    private val list = mutableListOf<Context>()

    fun add(context: Context): ResultSequence {
        when (context.node) {
            is ArrayNode -> context.node.forEach {
                list.add(Context(it, context.prev))
            }
            else -> list.add(context)
        }
        return this
    }

    fun back(steps: Int): ResultSequence {
        val rs = ResultSequence()
        list.forEach { context ->
            rs.add(context.back(steps))
        }
        return rs
    }

    fun filter(index: Int): ResultSequence {
        val rs = ResultSequence()
        val offset = if (index < 0) size + index else index
        if (offset in 0 until size) {
            rs.add(list[offset])
        }
        return rs
    }

    fun select(fieldName: String): ResultSequence {
        val rs = ResultSequence()
        list.filter { context ->
            context.node is ObjectNode && context.node.has(fieldName)
        }.forEach { context ->
            rs.add(Context(context.node[fieldName], context))
        }
        return rs
    }

    fun value(mapper: ObjectMapper = ObjectMapper()): JsonNode? {
        return when (size) {
            0 -> null
            1 -> list[0].node
            else -> mapper.createArrayNode().addAll(list.map { it.node })
        }
    }

    override val size: Int
        get() = list.size

    override fun contains(element: Context): Boolean {
        return list.contains(element)
    }

    override fun containsAll(elements: Collection<Context>): Boolean {
        return list.containsAll(elements)
    }

    override fun get(index: Int): Context {
        return list.get(index)
    }

    override fun indexOf(element: Context): Int {
        return list.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    override fun iterator(): Iterator<Context> {
        return list.iterator()
    }

    override fun lastIndexOf(element: Context): Int {
        return list.lastIndexOf(element)
    }

    override fun listIterator(): ListIterator<Context> {
        return list.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<Context> {
        return list.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<Context> {
        return list.subList(fromIndex, toIndex)
    }

} //~ Result