package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestArrayTypes {

    private val mapper = ObjectMapper()

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Array - exp`() {
        @Language("JSON")
        val expression =
            """
            [
              "value1",
              "value2"
            ]
            """
        val expected = mapper.readTree(expression)
        val actual = JSong(expression).evaluate()
        Assertions.assertEquals(expected, actual)
    }

}