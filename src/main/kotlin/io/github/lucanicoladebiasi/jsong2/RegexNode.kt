package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.node.TextNode
import java.util.regex.Pattern

class RegexNode(
    val pattern: Pattern,
): TextNode(pattern.pattern()) {

    companion object {

        fun ci(regex: String): RegexNode {
            return RegexNode(Pattern.compile(regex.substring(1, regex.length - 2), Pattern.CASE_INSENSITIVE))
        }

        fun ml(regex: String): RegexNode {
            return RegexNode(Pattern.compile(regex.substring(1, regex.length - 2), Pattern.MULTILINE))
        }

        fun of(regex: String): RegexNode {
            return RegexNode(Pattern.compile(regex.substring(1, regex.length - 1)))
        }

    } //~ companion

}