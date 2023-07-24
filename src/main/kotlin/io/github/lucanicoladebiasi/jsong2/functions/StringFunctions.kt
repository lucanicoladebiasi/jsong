package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong2.JSong
import io.github.lucanicoladebiasi.jsong2.RegexNode
import java.lang.IllegalArgumentException
import java.math.MathContext

/**
 * https://docs.jsonata.org/string-functions#string
 */
@Suppress("FunctionName", "unused", "UNUSED_PARAMETER")
class StringFunctions(private val mapper: ObjectMapper, private val mathContext: MathContext) {

    companion object {


        fun replace(str: String, pattern: Regex, replacement: String, limit: Int): String {
           return str
        }

        fun replace(str: String, pattern: String, replacement: String, limit: Int): String {
            when(pattern == replacement) {
                true -> return str
                else -> {
                    var txt = str
                    var countdown = limit
                    while (--countdown >= 0) {
                        when (txt.contains(pattern)) {
                            true -> txt = txt.replaceFirst(pattern, replacement)
                            else -> return txt
                        }

                    }
                    return txt
                }
            }
        }

    } //~ companion

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    fun `$string`(arg: JsonNode?): TextNode {
        return `$string`(arg, BooleanNode.FALSE)
    }

    /**
     * https://docs.jsonata.org/string-functions#string
     */
    fun `$string`(arg: JsonNode?, prettify: BooleanNode): TextNode {
        return when (arg) {
            null -> TextNode("")
            is NullNode -> TextNode("")
            is NumericNode -> when (arg.isNaN) {
                true -> throw IllegalArgumentException()
                else -> when (prettify.booleanValue()) {
                    true -> TextNode(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arg))
                    else -> TextNode(mapper.writeValueAsString(arg))
                }
            }

            is TextNode -> arg
            else -> when (prettify.booleanValue()) {
                true -> TextNode(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arg))
                else -> TextNode(mapper.writeValueAsString(arg))
            }
        }
    }

    /**
     * https://docs.jsonata.org/string-functions#length
     */
    fun `$length`(str: TextNode): IntNode {
        return IntNode(str.textValue().length)
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    fun `$substring`(str: TextNode, start: NumericNode): TextNode {
        val txt = str.textValue()
        val first = 0
            .coerceAtLeast(if (start.intValue() < 0) txt.length + start.intValue() else start.intValue())
            .coerceAtMost(txt.length)
        return TextNode(txt.substring(first))
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    fun `$substring`(str: TextNode, start: NumericNode, length: NumericNode): TextNode {
        val txt = str.textValue()
        val first = 0
            .coerceAtLeast(if (start.intValue() < 0) txt.length + start.intValue() else start.intValue())
            .coerceAtMost(txt.length)
        val last = 0
            .coerceAtLeast(length.let { first + length.asInt() })
            .coerceAtMost(txt.length)
        return TextNode(txt.substring(first, last))
    }

    /**
     * https://docs.jsonata.org/string-functions#substringbefore
     */
    fun `$substringBefore`(str: TextNode, chars: TextNode): TextNode {
        return TextNode(str.textValue().substringBefore(chars.textValue()))
    }

    /**
     * https://docs.jsonata.org/string-functions#substringafter
     */
    fun `$substringAfter`(str: TextNode, chars: TextNode): TextNode {
        return TextNode(str.textValue().substringAfter(chars.textValue()))
    }

    /**
     * https://docs.jsonata.org/string-functions#uppercase
     */
    fun `$uppercase`(str: TextNode): TextNode {
        return TextNode(str.textValue().uppercase())
    }

    /**
     * https://docs.jsonata.org/string-functions#lowercase
     */
    fun `$lowercase`(str: TextNode): TextNode {
        return TextNode(str.textValue().lowercase())
    }

    /**
     * https://docs.jsonata.org/string-functions#trim
     */
    fun `$trim`(str: TextNode): TextNode {
        return TextNode(str.textValue().replace(Regex("\\s+"), " ").trim())
    }

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    fun `$pad`(str: TextNode, width: NumericNode): TextNode {
        return `$pad`(str, width, TextNode(" "))
    }

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    fun `$pad`(str: TextNode, width: NumericNode, char: TextNode): TextNode {
        val size = width.asInt()
        val pad = char.textValue()[0]
        return TextNode(
            when (size < 0) {
                true -> str.textValue().padStart(-size, pad)
                else -> str.textValue().padEnd(size, pad)
            }
        )
    }

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    fun `$contains`(str: TextNode, pattern: TextNode): BooleanNode {
        return when (pattern) {
            is RegexNode -> BooleanNode.valueOf(str.textValue().contains(pattern.regex))
            else -> BooleanNode.valueOf(str.textValue().contains(pattern.textValue()))
        }
    }

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    fun `$split`(str: TextNode, separator: TextNode): ArrayNode {
        return `$split`(str, separator, IntNode(Int.MAX_VALUE))
    }

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    fun `$split`(str: TextNode, separator: TextNode, limit: NumericNode): ArrayNode {
        val array = mapper.createArrayNode()
        val list = when {
            separator is RegexNode -> str.textValue().split(separator.regex)
            else -> str.textValue().split(separator.textValue())
        }
        for (i in 0 until minOf(list.size, limit.asInt())) {
            array.add(TextNode(list[i]))
        }
        return array
    }

    /**
     * https://docs.jsonata.org/string-functions#join
     */
    fun `$join`(array: ArrayNode): TextNode {
        return `$join`(array, TextNode(("")))
    }

    /**
     * https://docs.jsonata.org/string-functions#join
     */
    fun `$join`(array: ArrayNode, separator: TextNode): TextNode {
        return TextNode(array.map { it.textValue() }.joinToString(separator.textValue()))
    }

    /**
     * https://docs.jsonata.org/string-functions#match
     */
    fun `$match`(str: TextNode, pattern: RegexNode): ArrayNode {
        return `$match`(str, pattern, IntNode(Int.MAX_VALUE))
    }

    /**
     * https://docs.jsonata.org/string-functions#match
     */
    fun `$match`(str: TextNode, pattern: RegexNode, limit: NumericNode): ArrayNode {
        val array = mapper.createArrayNode()
        val max = limit.asInt()
        pattern.regex.findAll(str.textValue()).forEachIndexed { index, matchResult ->
            if (index < max) {
                array.add(
                    MatchNode.of(
                        matchResult.value,
                        matchResult.range.first,
                        listOf(matchResult.groupValues.last()),
                        mapper
                    )
                )
            }
        }
        return array
    }

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    fun `$replace`(str: TextNode, pattern: TextNode, replacement: TextNode): TextNode {
        return `$replace`(str, pattern, replacement, IntNode(Int.MAX_VALUE))
    }

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    fun `$replace`(str: TextNode, pattern: TextNode, replacement: TextNode, limit: NumericNode): TextNode {
        return TextNode(when (pattern) {
            is RegexNode -> replace(str.textValue(), pattern.regex, replacement.textValue(), limit.asInt())
            else -> replace(str.textValue(), pattern.textValue(), replacement.textValue(), limit.asInt())
        })
    }

    /**
     * https://docs.jsonata.org/string-functions#eval
     */
    fun `$eval`(expr: TextNode): JsonNode? {
        return `$eval`(expr, null)
    }

    /**
     * https://docs.jsonata.org/string-functions#eval
     */
    fun `$eval`(expr: TextNode, context: JsonNode?): JsonNode? {
        return JSong(expr.textValue(), mapper, mathContext).evaluate(context)
    }

    /**
     * https://docs.jsonata.org/string-functions#base64encode
     */
    fun `$base64encode`(str: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#base64decode
     */
    fun `$base64decode`(str: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#encodeurlcomponent
     */
    fun `$encodeUrlComponent`(str: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#encodeurl
     */
    fun `$encodeUrl`(str: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#decodeurlcomponent
     */
    fun `$decodeUrlComponent`(str: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#decodeurl
     */
    fun `$decodeUrl`(str: TextNode): TextNode {
        TODO()
    }


}