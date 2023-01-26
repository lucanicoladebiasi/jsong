package org.jsonic

import org.junit.jupiter.api.Test

class Test {

    @Test
    fun test() {
        //val expression = "library.books#\$i[\"Kernighan\" in authors].{\"title\": title, \"index\": \$i }"
        val expression = "library.loans@\$l.books"

        val actual = Processor(TestResources.library).evaluate(expression)
        println(actual)
    }

}