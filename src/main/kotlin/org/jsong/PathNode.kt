package org.jsong

import com.fasterxml.jackson.databind.node.TextNode

class PathNode(tag: String) : TextNode(normalize(tag)) {

    companion object {

        private const val BACKTICK = '`'

        private fun normalize(tag: String): String {
            return when {
                tag[0] == BACKTICK && tag[tag.length - 1] == BACKTICK -> tag.substring(1, tag.length - 1)
                else -> tag
            }
        }

    } //~ companion

} //~ PathNode