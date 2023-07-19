package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.util.regex.Pattern

class RegexNode(
    val pattern: Pattern,
    nf: JsonNodeFactory
): ObjectNode(nf, mapOf<String, TextNode>(Pair(PATTERN_TAG, TextNode(pattern.pattern())))) {

    companion object {

        const val PATTERN_TAG = "pattern"

        fun ci(regex: String, nf: JsonNodeFactory = ObjectMapper().nodeFactory): RegexNode {
            return RegexNode(Pattern.compile(regex.substring(1, regex.length - 2), Pattern.CASE_INSENSITIVE), nf)
        }

        fun ml(regex: String, nf: JsonNodeFactory = ObjectMapper().nodeFactory): RegexNode {
            return RegexNode(Pattern.compile(regex.substring(1, regex.length - 2), Pattern.MULTILINE), nf)
        }

        fun of(regex: String, nf: JsonNodeFactory = ObjectMapper().nodeFactory): RegexNode {
            return RegexNode(Pattern.compile(regex.substring(1, regex.length - 1)), nf)
        }

    } //~ companion

}