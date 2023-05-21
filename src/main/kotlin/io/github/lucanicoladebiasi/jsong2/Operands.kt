package io.github.lucanicoladebiasi.jsong2

class Operands: Stack<Sequence>() {

    override fun pop(): Sequence {
        return super.pop()!!
    }

    override fun push(element: Sequence): Sequence {
        return super.push(element)
    }


}