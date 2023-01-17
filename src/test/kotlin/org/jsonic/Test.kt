package org.jsonic

import org.junit.jupiter.api.Test

class Test {

    @Test
    fun test() {
        val actual = Processor(TestResources.address).evaluate("\$exists(Other.Nothing)")
        println(actual)
    }

}