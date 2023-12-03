package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.node.TextNode

class RegexNode(
    val regex: Regex,
): TextNode(regex.pattern) {

    companion object {

        fun ci(pattern: String): RegexNode {
            return RegexNode(Regex(pattern, RegexOption.IGNORE_CASE))
        }

        fun ml(pattern: String): RegexNode {
            return RegexNode(Regex(pattern, RegexOption.MULTILINE))
        }

        fun of(pattern: String): RegexNode {
            return RegexNode(Regex(pattern))
        }

    } //~ companion

}