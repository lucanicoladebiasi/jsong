package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.node.TextNode
import java.util.regex.Pattern

class RegexNode(
    val pattern: Pattern,
    val regex: String
): TextNode(regex) {

    companion object {

        fun ci(regex: String): RegexNode {
            return RegexNode(Pattern.compile(regex.substring(1, regex.length - 2), Pattern.CASE_INSENSITIVE), regex)
        }

        fun ml(regex: String): RegexNode {
            return RegexNode(Pattern.compile(regex.substring(1, regex.length - 2), Pattern.MULTILINE), regex)
        }

        fun of(regex: String): RegexNode {
            return RegexNode(Pattern.compile(regex.substring(1, regex.length - 1)), regex)
        }

    } //~ companion

}