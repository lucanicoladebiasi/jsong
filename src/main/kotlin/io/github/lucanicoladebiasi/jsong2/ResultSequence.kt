package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class ResultSequence(element: Context? = null) : List<Context> {

    private val list = mutableListOf<Context>()

    init {
        element?.let { list.add(element) }
    }

    val indexes: Set<Int>
        get() {
            val set = mutableSetOf<Int>()
            list.filter { it.node is RangeNode }.forEach {
                set.addAll((it.node as RangeNode).indexes)
            }
            return set.sorted().toSet()
        }

//    fun add(element: Context): ResultSequence {
//        when(element.node) {
//            is ArrayNode -> element.node.forEach { list.add(Context(it, element.prev)) }
//            else -> list.add(element)
//        }
//        return this
//    }

    fun add(element: Context): ResultSequence {
        list.add(element)
        return this
    }

    fun add(resultSequence: ResultSequence): ResultSequence {
        list.addAll(resultSequence.list)
        return this
    }

    fun value(nf: JsonNodeFactory = ObjectMapper().nodeFactory): JsonNode? {
        return when (size) {
            0 -> null
            1 -> list[0].node
            else -> ArrayNode(nf).addAll(list.map { it.node })
        }
    }

    override val size: Int
        get() = list.size

    override fun get(index: Int): Context {
        return list[index]
    }

    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    override fun iterator(): Iterator<Context> {
        return list.iterator()
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

    override fun lastIndexOf(element: Context): Int {
        return list.lastIndexOf(element)
    }

    override fun indexOf(element: Context): Int {
        return list.lastIndexOf(element)
    }

    override fun containsAll(elements: Collection<Context>): Boolean {
        return list.containsAll(elements)
    }

    override fun contains(element: Context): Boolean {
        return list.contains(element)
    }

}