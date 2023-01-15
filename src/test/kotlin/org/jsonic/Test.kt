package org.jsonic

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Test {

    @Test
    fun `in (Inclusion) - in predicate`() {
        //val expression = "library.books[\"Aho\" in authors].title"
        val expression = "library.books[\"Aho\" in authors].title"

        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [
              "The AWK Programming Language",
              "Compilers: Principles, Techniques, and Tools"
            ]
        """.trimIndent()
        )
        val actual = Processor(TestResources.library).evaluate(expression)
        println(actual)
    }

}