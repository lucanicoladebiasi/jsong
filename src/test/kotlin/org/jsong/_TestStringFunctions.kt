package org.jsong

import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.TextNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

/**
 * https://docs.jsonata.org/string-functions
 */
class _TestStringFunctions {
    
    /**
     * https://docs.jsonata.org/string-functions#string
     */
    @Test
    fun `$string() - numeric`() {
        val expression = "\$string(5)"
        val actual = _JSong.of(expression).evaluate()
        val expected = TextNode("5")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    @Test
    fun `$string() - array`() {
        val expression = "[1..5].\$string()"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("[\"1\", \"2\", \"3\", \"4\", \"5\"]")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#length
     */
    @Test
    fun `$length()`() {
        val expression = "\$length(\"Hello World\")"
        val actual = _JSong.of(expression).evaluate()
        val expected = DecimalNode(BigDecimal(11))
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - positive start`() {
        val expression = "\$substring(\"Hello World\", 3)"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("\"lo World\"")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - positive start and length`() {
        val expression = "\$substring(\"Hello World\", 3, 5)"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("\"lo Wo\"")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - negative start`() {
        val expression = "\$substring(\"Hello World\", -4)"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("\"orld\"")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    @Test
    fun `$substring() - negative start and length`() {
        val expression = "\$substring(\"Hello World\", -4, 2)"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("\"or\"")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substringbefore
     */
    @Test
    fun `$substringBefore()`() {
        val expression = "\$substringBefore(\"Hello World\", \" \")"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("\"Hello\"")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#substringafter
     */
    @Test
    fun `$substringAfter`() {
        val expression = "\$substringAfter(\"Hello World\", \" \")"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("\"World\"")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#uppercase
     */
    @Test
    fun `$uppercase()`() {
        val expression = "\$uppercase(\"Hello World\")"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("\"HELLO WORLD\"")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#lowercase
     */
    @Test
    fun `$lowercase()`() {
        val expression = "\$lowercase(\"Hello World\")"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("\"hello world\"")
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#trim
     */
    @Test
    fun `$trim()`() {
        val expression = "\$trim(\" Hello   World \")"
        val actual = _JSong.of(expression).evaluate()
        val expected = _TestResources.mapper.readTree("\"Hello World\"")
        assertEquals(expected, actual)
    }


    /**
     * https://docs.jsonata.org/string-functions#trim
     */
    @Test
    @Disabled("new line parsed as \$trim(\"World\"")
    fun `$trim() - new line`() {
        val expression = "\$trim(\" Hello \n World \")"
        val expected = _TestResources.mapper.readTree("\"Hello World\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    @Test
    fun `$pad() - positive width`() {
        val expression = "\$pad(\"foo\", 5)"
        val expected = _TestResources.mapper.readTree("\"foo  \"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    @Test
    fun `$pad() - negative width`() {
        val expression = "\$pad(\"foo\", -5)"
        val expected = _TestResources.mapper.readTree("\"  foo\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    @Test
    fun `$pad() - negative width with char`() {
        val expression = "\$pad(\"foo\", -5, \"#\")"
        val expected = _TestResources.mapper.readTree("\"##foo\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    @Test
    @Disabled("to do ~> operator")
    fun `$pad() - width and char`() {
        val expression = "\$formatBase(35, 2) ~> \$pad(-8, '0')"
        val expected = _TestResources.mapper.readTree("\"00100011\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains`() {
        val expression = "\$contains(\"abracadabra\", \"bra\")"
        val expected = _TestResources.mapper.readTree("true")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - regex positive`() {
        val expression = "\$contains(\"abracadabra\", /a.*a/)"
        val expected = BooleanNode.TRUE
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - regex negative`() {
        val expression = "\$contains(\"abracadabra\", /ar.*a/"
        val expected = BooleanNode.FALSE
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - regex`() {
        val expression = "\$contains(\"Hello World\", /wo/)"
        val expected = BooleanNode.FALSE
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - regex with options`() {
        val expression = "\$contains(\"Hello World\", /wo/i)"
        val expected = BooleanNode.TRUE
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    @Test
    fun `$contains - regex in filter`() {
        val expression = "Phone[\$contains(number, /^077/)]"
        val expected = _TestResources.mapper.readTree("{ \"type\": \"mobile\", \"number\": \"077 7700 1234\" }")
        val actual = _JSong.of(expression).evaluate(_TestResources.address)
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    @Test
    fun `$split`() {
        val expression = "\$split(\"so many words\", \" \")"
        val expected = _TestResources.mapper.readTree("[ \"so\", \"many\", \"words\" ]")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    @Test
    fun `$split - limit`() {
        val expression = "\$split(\"so many words\", \" \", 2)"
        val expected = _TestResources.mapper.readTree("[ \"so\", \"many words\" ]") // strict JSong returns [ "so", "many" ]
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    @Test
    fun `$split - regex`() {
        val expression = "\$split(\"too much, punctuation. hard; to read\", /[ ,.;]+/)"
        val expected = _TestResources.mapper.readTree("[\"too\", \"much\", \"punctuation\", \"hard\", \"to\", \"read\"]")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#join
     */
    @Test
    fun `$join()`() {
        val expression = "\$join([\"a\",\"b\",\"c\"])"
        val expected = _TestResources.mapper.readTree("\"abc\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#join
     */

    @Test
    @Disabled("to do ~> operator")
    fun `$join() - chain with separator`() {
        val expression = "\$split(\"too much, punctuation. hard; to read\", /[ ,.;]+/, 3) ~> \$join(', ')"
        val expected = _TestResources.mapper.readTree("\"too, much, punctuation\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }


    /**
     * https://docs.jsonata.org/string-functions#match
     */
    @Test
    fun `$match()`(){
        val expression = "\$match(\"ababbabbcc\",/a(b+)/)"
        @Language("JSON")
        val expected = _TestResources.mapper.readTree("""
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
        """.trimIndent())
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    @Test
    fun `$replace() - pattern, replacement`() {
        val expression = "\$replace(\"John Smith and John Jones\", \"John\", \"Mr\")"
        val expected = _TestResources.mapper.readTree("\"Mr Smith and Mr Jones\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#replace
     */

    @Test
    @Disabled("to do limit")
    fun `$replace() - pattern, pattern, replacement, limit`() {
        val expression = "\$replace(\"John Smith and John Jones\", \"John\", \"Mr\", 1)"
        val expected = _TestResources.mapper.readTree("\"Mr Smith and John Jones\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    @Test
    fun `$replace() - regex pattern, replacement `() {
        val expression = "\$replace(\"abracadabra\", /a.*?a/, \"*\")"
        val expected = _TestResources.mapper.readTree("\"*c*bra\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    @Test
    fun `$replace() - regex pattern, context replacement`() {
        val expression = "\$replace(\"John Smith\", /(\\w+)\\s(\\w+)/, \"\$2, \$1\")"
        val expected = _TestResources.mapper.readTree("\"Smith, John\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    @Test
    @Disabled("$$$1 throw illegal group reference")
    fun `$replace() - regex pattern, encoded context replacement `() {
        val expression = "\$replace(\"265USD\", /([0-9]+)USD/, \"\$\$\$1\")"
        val expected = TextNode("$265")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    @Test
    @Disabled("to do implement functions")
    fun `$replace() - function`() {
        val expression = """
            (
              ${'$'}convert := function(${'$'}m) {
                (${'$'}number(${'$'}m.groups[0]) - 32) * 5/9 & "C"
              };
              ${'$'}replace("temperature = 68F today", /(\d+)F/, ${'$'}convert)
            )
        """.trimIndent()
        val expected = _TestResources.mapper.readTree("\"temperature = 20C today\"\n")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#eval
     */
    @Test
    fun `$eval()`() {
        val expression = "\$eval(\"[1,2,3]\")"
        val expected = _JSong.of("[1, 2, 3]").evaluate()
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#eval
     */
    @Test
    fun `$eval() - call function`() {
        val expression = "\$eval(\"[1,\$string(2),3]\")"
        val expected = _TestResources.mapper.createArrayNode()
            .add(DecimalNode(BigDecimal(1)))
            .add("2")
            .add(DecimalNode(BigDecimal(3)))
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }


    /**
     * https://docs.jsonata.org/string-functions#base64encode
     */
    @Test
    fun `$base64encode()`() {
        val expression = "\$base64encode(\"myuser:mypass\")"
        val expected = _TestResources.mapper.readTree("\"bXl1c2VyOm15cGFzcw==\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#base64decode
     */
    @Test
    fun `$base64decode()`() {
        val expression = "\$base64decode(\"bXl1c2VyOm15cGFzcw==\")"
        val expected = _TestResources.mapper.readTree("\"myuser:mypass\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#encodeurlcomponent
     */
    @Test
    fun `$encodeUrlComponent()`() {
        val expression = "\$encodeUrlComponent(\"?x=test\")"
        val expected = _TestResources.mapper.readTree("\"%3Fx%3Dtest\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#encodeurl
     */
    @Test
    fun `$encodeUrl()`() {
        val expression = "\$encodeUrl(\"https://mozilla.org/?x=шеллы\")"
        val expected = _TestResources.mapper.readTree("\"https%3A%2F%2Fmozilla.org%2F%3Fx%3D%D1%88%D0%B5%D0%BB%D0%BB%D1%8B\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#decodeurlcomponent
     */
    @Test
    fun `$decodeUrlComponent()`() {
        val expression = "\$decodeUrlComponent(\"%3Fx%3Dtest\")"
        val expected = _TestResources.mapper.readTree("\"?x=test\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

    /**
     * https://docs.jsonata.org/string-functions#decodeurl
     */
    @Test
    fun `$decodeUrl()`() {
        val expression = "\$decodeUrl(\"https://mozilla.org/?x=%D1%88%D0%B5%D0%BB%D0%BB%D1%8B\")"
        val expected = _TestResources.mapper.readTree("\"https://mozilla.org/?x=шеллы\"")
        val actual = _JSong.of(expression).evaluate()
        assertEquals(expected, actual)
    }

}