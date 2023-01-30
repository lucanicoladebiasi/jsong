package org.jsonic

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

class Test {

    val w = ObjectMapper().writerWithDefaultPrettyPrinter()

    @Test
    fun test() {
        //val expression = "library.books#\$i[\"Kernighan\" in authors].{\"title\": title, \"index\": \$i }"
        //val expression = "library.loans@\$l.{'L': \$l }"
        val expression = "library.loans@\$L.{'due': \$L.return}"

        val actual = Processor(TestResources.library).evaluate(expression)
        println(w.writeValueAsString(actual))

    }

}