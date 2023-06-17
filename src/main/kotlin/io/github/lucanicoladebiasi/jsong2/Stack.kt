package io.github.lucanicoladebiasi.jsong2

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