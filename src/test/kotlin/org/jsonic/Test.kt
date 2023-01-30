package org.jsonic

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Test {

    val w = ObjectMapper().writerWithDefaultPrettyPrinter()

    @Test
    fun test() {
        //val expression = "library.books#\$i[\"Kernighan\" in authors].{\"title\": title, \"index\": \$i }"
        //val expression = "library.loans@\$l.{'L': \$l }"

        //val actual = Processor(TestResources.library).evaluate(expression)
        //println(w.writeValueAsString(actual))
        val expression = "(\$volume := function(\$l, \$w, \$h){ \$l * \$w * \$h }; \$volume(10, 10, 5))"
        val expected = Processor().evaluate("500")
        val actual = Processor().evaluate(expression)
        println(actual)
    }

}