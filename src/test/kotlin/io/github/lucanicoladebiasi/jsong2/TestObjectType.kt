package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class TestObjectType {

    private val mapper = ObjectMapper()

    /**
     * https://docs.jsonata.org/construction#json-literals
     */
    @Test
    fun `Literal - object`() {
        @Language("JSON")
        val expression = """
        {
            "key1": "value1",
            "key2": "value2"
        }
        """.trimIndent()
        val expected = mapper.readTree(expression)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}