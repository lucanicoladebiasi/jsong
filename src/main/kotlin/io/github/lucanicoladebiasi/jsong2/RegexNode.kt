package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.node.TextNode

class RegexNode(
    val regex: Regex,
): TextNode(regex.pattern) {

    companion object {

        fun ci(pattern: String): RegexNode {
            return RegexNode(Regex(pattern.substring(1, pattern.length - 2), RegexOption.IGNORE_CASE))
        }

        fun ml(pattern: String): RegexNode {
            return RegexNode(Regex(pattern.substring(1, pattern.length - 2), RegexOption.MULTILINE))
        }

        fun of(pattern: String): RegexNode {
            return RegexNode(Regex(pattern.substring(1, pattern.length - 1)))
        }

    } //~ companion

}