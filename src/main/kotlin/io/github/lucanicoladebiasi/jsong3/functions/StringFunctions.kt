package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode

class StringFunctions {

    companion object {

        fun stringOf(node: JsonNode?, writer: ObjectWriter): String {
            return when(node) {
                null -> ""
                is NullNode -> ""
                is TextNode -> node.textValue()
                else -> writer.writeValueAsString(node)
            }
        }

    } //~ companion

} //~ StringFunctions