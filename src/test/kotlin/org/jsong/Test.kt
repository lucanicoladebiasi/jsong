package org.jsong

import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Test {

    val w = ObjectMapper().writerWithDefaultPrettyPrinter()

    @Test
    fun test() {
        val expression = "library.books#\$i[\"Kernighan\" in authors].{\"title\": title, \"index\": \$i }"


        @Language("JSON")
        val expected = TestResources.mapper.readTree(
            """
            [
              {
                "title": "The C Programming Language",
                "index": 1
              },
              {
                "title": "The AWK Programming Language",
                "index": 2
              }
            ]
            """.trimIndent()
        )
        val actual = Processor(TestResources.library).evaluate(expression)
        assertEquals(expected, actual)

    }

}