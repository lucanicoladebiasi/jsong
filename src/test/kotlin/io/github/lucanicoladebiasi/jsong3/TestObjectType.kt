package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestObjectType {

    private val om = ObjectMapper()

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
        val expected = om.readTree(expression)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

}