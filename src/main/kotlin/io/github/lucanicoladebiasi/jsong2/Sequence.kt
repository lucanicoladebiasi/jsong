package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class Sequence : List<Context> {


    private val list = mutableListOf<Context>()

    fun add(element: Context): Sequence {
        list.add(element)
        return this
    }

    fun json(mapper: ObjectMapper = ObjectMapper()): JsonNode? {
        return when(size) {
            0 -> null
            1 -> list[0].node
            else -> mapper.createArrayNode().addAll(list.map { it.node })
        }
    }

    override val size: Int
        get() = list.size

    override fun get(index: Int): Context {
        return list.get(index)
    }

    override fun contains(element: Context): Boolean {
        return list.contains(element)
    }

    override fun containsAll(elements: Collection<Context>): Boolean {
        return list.containsAll(elements)
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

} //~ Sequence
