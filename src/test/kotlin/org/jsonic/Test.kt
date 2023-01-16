package org.jsonic

import org.junit.jupiter.api.Test

class Test {

    @Test
    fun test() {
        Processor().evaluate("\$count([1,2,3,1])")
    }

}