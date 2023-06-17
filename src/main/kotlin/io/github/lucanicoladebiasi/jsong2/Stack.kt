package io.github.lucanicoladebiasi.jsong2

class Stack: Collection<ResultSequence> { //~ Stack

    private val deck = ArrayDeque<ResultSequence>()

    override val size: Int
        get() = deck.size

    fun peek(): ResultSequence {
        return deck.firstOrNull() ?: ResultSequence()
    }

    fun pop(): ResultSequence {
        return deck.removeFirstOrNull() ?: ResultSequence()
    }

    fun push(element: ResultSequence): ResultSequence {
        deck.addFirst(element)
        return element
    }

    override fun contains(element: ResultSequence): Boolean {
        return deck.contains(element)
    }

    override fun containsAll(elements: Collection<ResultSequence>): Boolean {
        return deck.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return deck.isEmpty()
    }

    override fun iterator(): Iterator<ResultSequence> {
        return deck.iterator()
    }

}