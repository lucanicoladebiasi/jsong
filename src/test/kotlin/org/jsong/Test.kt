package org.jsong

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

class Test {

    val w = ObjectMapper().writerWithDefaultPrettyPrinter()

    @Test
    fun test() {
        val expression = "(\$volume := function(\$l, \$w, \$h){ \$l * \$w * \$h }; \$boolean(\$volume))"
        val actual = Processor().evaluate(expression)!!.booleanValue()
        println(w.writeValueAsString(actual))

    }

}