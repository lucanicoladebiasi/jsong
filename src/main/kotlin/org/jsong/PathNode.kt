package com.tradeix.jsonata

import com.fasterxml.jackson.databind.node.TextNode

/**
 * Represent the element of the path to select a JSON node in JSONata.
 *
 * **NOTE:** this class is an extension of the JSONATA data type.
 *
 * @param tag name of the property to select in the JSON node.
 */
class TagNode(tag: String) : TextNode(normalize(tag)) {

    companion object {

        /**
         * Character to delimit tags (JSON properties) including JSONata keywords.
         */
        private const val BACKTICK = '`'

        /**
         * If [tag] starts and ends with [BACKTICK], remove them from thr returned text.
         */
        private fun normalize(tag: String): String {
            return when {
                tag[0] == BACKTICK && tag[tag.length - 1] == BACKTICK -> tag.substring(1, tag.length - 1)
                else -> tag
            }
        }

    } //~ companion

} //~ TagNode