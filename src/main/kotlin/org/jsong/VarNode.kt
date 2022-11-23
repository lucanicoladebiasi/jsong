package org.jsong

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

class VarNode(
    name: String,
    value: ArrayNode,
    nodeFactory: JsonNodeFactory
): ObjectNode(nodeFactory, mapOf(Pair(NAME_TAG, TextNode(name)), Pair(VALUE_TAG, value))) {

    companion object {

        const val NAME_TAG = "name"

        const val VALUE_TAG = "value"

        fun of(name: String, value: ArrayNode, nodeFactory: JsonNodeFactory = ObjectMapper().nodeFactory): VarNode {
            return VarNode(name, value, nodeFactory)
        }

    } //~ companion

    val name get() = this[NAME_TAG]

    val value get() = this[VALUE_TAG]

} //~ VarNode