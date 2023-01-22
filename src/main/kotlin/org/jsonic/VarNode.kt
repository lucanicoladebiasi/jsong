package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

open class VarNode(
    name: String,
    value: JsonNode?,
    nodeFactory: JsonNodeFactory = ObjectMapper().nodeFactory
): ObjectNode(nodeFactory, mapOf(Pair(NAME_TAG, TextNode(name)), Pair(VALUE_TAG, value))) {

    companion object {

        const val NAME_TAG = "name"

        const val VALUE_TAG = "value"

    } //~ companion

    val name get() = this[NAME_TAG] as TextNode

    val value get() = this[VALUE_TAG]

} //~ VarNode
