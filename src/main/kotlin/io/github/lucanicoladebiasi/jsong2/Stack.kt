package io.github.lucanicoladebiasi.jsong2

open class Stack<T>() : AbstractMutableList<T>() {

    override val size: Int
        get() = deck.size

    private val deck = ArrayDeque<T>()
    override fun add(index: Int, element: T) {
        deck.add(index, element)
    }

    override fun get(index: Int): T {
        return deck.get(index)
    }

    open fun pop(): T? {
        return deck.removeLastOrNull()
    }

    open fun push(element: T): T {
        deck.addLast(element)
        return element
    }

    override fun removeAt(index: Int): T {
        return deck.removeAt(index)
    }

    override fun set(index: Int, element: T): T {
        return deck.set(index, element)
    }

}