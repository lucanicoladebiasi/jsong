package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong2.RegexNode

/**
 * https://docs.jsonata.org/string-functions#string
 */
@Suppress("FunctionName", "unused", "UNUSED_PARAMETER")
class StringFunctions: Library() {


    /**
     * https://docs.jsonata.org/string-functions#string
     */
    fun `$string`(arg: JsonNode?, prettify: BooleanNode = BooleanNode.FALSE): TextNode {
        TODO()
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
        return TextNode(str.textValue().substring(start.asInt()))
    }

    /**
     * https://docs.jsonata.org/string-functions#substring
     */
    fun `$substring`(str: TextNode, start: NumericNode, length: NumericNode): TextNode {
        return TextNode(str.textValue().substring(start.asInt(), length.asInt()))
    }

    /**
     * https://docs.jsonata.org/string-functions#substringbefore
     */
    fun `$substringBefore`(str: TextNode, chars: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#substringafter
     */
    fun `$substringAfter`(str: TextNode, chars: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#uppercase
     */
    fun `$uppercase`(str: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#lowercase
     */
    fun `$lowercase`(str: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#trim
     */
    fun `$trim`(str: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#pad
     */
    fun `$pad`(str: TextNode, width: IntNode, char: TextNode = TextNode(" ")): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    fun `$contains`(str: TextNode, pattern: RegexNode): BooleanNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#contains
     */
    fun `$contains`(str: TextNode, pattern: TextNode): BooleanNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#split
     */
    fun `$split`(str: TextNode, separator: TextNode, limit: IntNode = IntNode(Int.MAX_VALUE)): ArrayNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#join
     */
    fun `$join`(array: ArrayNode, separator: TextNode = TextNode("")): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#match
     */
    fun `$match`(str: TextNode, pattern: RegexNode, limit: IntNode = IntNode(Int.MAX_VALUE)): ArrayNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#replace
     */
    fun `$replace`(str: TextNode, pattern: RegexNode, replacement: TextNode, limit: IntNode = IntNode(Int.MAX_VALUE)): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/string-functions#eval
     */
    fun `$eval`(expr: TextNode, context: JsonNode? = null) {
        TODO()
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