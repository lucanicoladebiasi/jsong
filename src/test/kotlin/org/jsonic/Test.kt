package org.jsonic

import org.jsong.TestResources
import org.junit.jupiter.api.Test

class Test {

    @Test
    fun `Returns a JSON string`() {
        val expression = "Phone[0]"
        val actual = Interpreter(TestResources.address).evaluate(expression)
        println(actual)
    }

}