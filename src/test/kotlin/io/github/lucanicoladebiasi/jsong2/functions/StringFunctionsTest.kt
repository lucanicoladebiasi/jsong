package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong2.JSong
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StringFunctionsTest {

    private val mapper = ObjectMapper()

    private var node: JsonNode? = null

    @BeforeAll
    fun setUp() {
        node = mapper.readTree(Thread.currentThread().contextClassLoader.getResource("address.json"))
    }

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    @Test
    fun `$string - NaN`() {
        assertThrows(IllegalArgumentException::class.java) {
            StringFunctions(mapper).`$string`(DoubleNode(Double.NaN))
        }
    }

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    @Test
    fun `$string - null`() {
        val expression = "\$string(null)"
        val expected = TextNode("")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    @Test
    fun `$string - numeric`() {
        val expression = "\$string(5)"
        val expected = TextNode("5")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    @Test
    fun `$string - no prettify`() {
        val expression = "[1..5].\$string()"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            ["1", "2", "3", "4", "5"]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    @Test
    fun `$string - prettify`() {
        val expression = "\$string([1..5], true)"
        val expected = "{\n  \"max\" : 5,\n  \"min\" : 1\n}"
        val actual = JSong(expression).evaluate()?.textValue()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#length
     */
    @Test
    fun `$length`() {
        val expression = "\$length(\"Hello World\")"
        val expected = IntNode(11)
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring - positive start`() {
        val expression = "\$substring(\"Hello World\", 3)"
        val expected = TextNode("lo World")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - positive start and length`() {
        val expression = "\$substring(\"Hello World\", 3, 5)"
        val expected = TextNode("lo Wo")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - negative start`() {
        val expression = "\$substring(\"Hello World\", -4)"
        val expected = TextNode("orld")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - negative start and length`() {
        val expression = "\$substring(\"Hello World\", -4, 2)"
        val expected = TextNode("or")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substringbefore
     */
    @Test
    fun `$substringBefore`() {
        val expression = "\$substringBefore(\"Hello World\", \" \")"
        val expected = TextNode("Hello")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substringafter
     */
    @Test
    fun `$substringAfter`() {
        val expression = "\$substringAfter(\"Hello World\", \" \")"
        val expected = TextNode("World")
        val actual = JSong(expression).evaluate()
        kotlin.test.assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#uppercase
     */
    @Test
    fun `$uppercase`() {
        val expression = "\$uppercase(\"Hello World\")"
        val expected = TextNode("HELLO WORLD")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#lowercase
     */
    @Test
    fun `$lowercase`() {
        val expression = "\$lowercase(\"Hello World\")"
        val expected = TextNode("hello world")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#trim
     */
    @Test
    fun `$trim`() {
        val expression = "\$trim(\" Hello \n World \")"
        val expected = TextNode("Hello World")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#pad
     */
    @Test
    fun `$pad - right - no pad char`() {
        val expression = "\$pad(\"foo\", 5)"
        val expected = TextNode("foo  ")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    @Test
    fun `$pad - left - no pad char`() {
        val expression = "\$pad(\"foo\", -5)"
        val expected = TextNode("  foo")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    @Test
    fun `$pad - right - pad char`() {
        val expression = "\$pad(\"foo\", -5, \"#\")"
        val expected = TextNode("##foo")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    @Test
    @Disabled
    fun `$pad - right - pad char - chained`() {
        val expression = "\$formatBase(35, 2) ~> \$pad(-8, '0')"
        val expected = TextNode("00100011")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - string`() {
        val expression = "\$contains(\"abracadabra\", \"bra\")"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - regex - true`() {
        val expression = "\$contains(\"abracadabra\", /a.*a/)"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - regex - false`() {
        val expression = "\$contains(\"abracadabra\", /ar.*a/)"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - regex - case sensitive`() {
        val expression = "\$contains(\"Hello World\", /wo/)"
        val expected = BooleanNode.FALSE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - regex - case insensitive`() {
        val expression = "\$contains(\"Hello World\", /wo/i)"
        val expected = BooleanNode.TRUE
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - filter`() {
        val expression = "Phone[\$contains(number, /^077/)]"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            { 
                "type": "mobile", 
                "number": "077 7700 1234" 
            } 
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate(node)
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#split
     */
    @Test
    fun `$split - no limit`() {
        val expression = "\$split(\"so many words\", \" \")"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            [ "so", "many", "words" ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#split
     */
    @Test
    fun `$split - limited`() {
        val expression = "\$split(\"so many words\", \" \", 2)"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            ["so", "many"]        
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#split
     */
    @Test
    fun `$split - regex`() {
        val expression = "\$split(\"too much, punctuation. hard; to read\", /[\\s,.;]+/)"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            ["too", "much", "punctuation", "hard", "to", "read"]
            """
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#join
     */
    @Test
    fun `$join - no separator`() {
        val expression = "\$join(['a','b','c'])"
        val expected = TextNode("abc")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * http://docs.jsonata.org/string-functions#join
     */
    @Test
    fun `$join - with separator`() {
        val expression = "\$split(\"too much, punctuation. hard; to read\", /[\\s,.;]+/, 3) ~> \$join(', ')"
        val expected = TextNode("too, much, punctuation")
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }


    @Test
    fun `$match`() {
        val expression = "\$match(\"ababbabbcc\",/a(b+)/)"

        @Language("JSON")
        val expected = mapper.readTree(
            """
            [
              {
                "match": "ab",
                "index": 0,
                "groups": ["b"]
              },
              {
                "match": "abb",
                "index": 2,
                "groups": ["bb"]
              },
              {
                "match": "abb",
                "index": 5,
                "groups": ["bb" ]
              }
            ]
            """.trimIndent()
        )
        val actual = JSong(expression).evaluate()
        assertEquals(expected, actual)
    }

    @Test
    fun `$replace`() {
    }

    @Test
    fun `$eval`() {
    }

    @Test
    fun `$base64encode`() {
    }

    @Test
    fun `$base64decode`() {
    }

    @Test
    fun `$encodeUrlComponent`() {
    }

    @Test
    fun `$encodeUrl`() {
    }

    @Test
    fun `$decodeUrlComponent`() {
    }

    @Test
    fun `$decodeUrl`() {
    }
}