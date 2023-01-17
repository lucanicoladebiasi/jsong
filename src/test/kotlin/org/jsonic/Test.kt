package org.jsonic

import org.junit.jupiter.api.Test

class Test {

    @Test
    fun test() {
        println(Processor().evaluate("\$count([1,2,3,1])"))
    }

}