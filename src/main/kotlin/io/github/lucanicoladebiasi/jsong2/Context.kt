package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

data class Context(
    val node: JsonNode,
    val prev: Context? = null
)

class Sequence: List<Context> {


    private val list = mutableListOf<Context>()

    override val size: Int
        get() = list.size

    fun add(element: Context): Sequence {
        list.add(element)
        return this
    }

    override fun contains(element: Context): Boolean {
        return list.contains(element)
    }

    override fun containsAll(elements: Collection<Context>): Boolean {
        return list.containsAll(elements)
    }

    override fun get(index: Int): Context {
        return list[index]
    }

    fun get(fieldName: String): Sequence {
        val sequence = Sequence()
        list.filter { context ->
            context.node is ObjectNode
        }.filter  { context  ->
            context.node.has(fieldName)
        }.forEach { context ->
            sequence.add(Context(context.node.get(fieldName), context))
        }
        return sequence
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

    fun json(mapper: ObjectMapper = ObjectMapper()): JsonNode? {
        return when(size) {
            0 -> null
            1 -> list[0].node
            else -> mapper.createArrayNode().addAll(list.map { it.node })
        }
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

} //~ Sequence

class Stack {

    private val deck = ArrayDeque<Sequence>()

    fun peek(): Sequence {
        return deck.firstOrNull() ?: Sequence()
    }

    fun pop(): Sequence {
        return deck.removeFirstOrNull() ?: Sequence()
    }

    fun push(element: Sequence): Sequence {
        deck.addFirst(element)
        return element
    }

} //~ Stack