package org.jsong

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

class Test {

    val w = ObjectMapper().writerWithDefaultPrettyPrinter()

    @Test
    fun test() {
        val expression =
            "library.loans@\$L.books@\$B[\$L.isbn=\$B.isbn]"

        val actual = Processor(TestResources.library).evaluate(expression)
        println(w.writeValueAsString(actual))

    }

}