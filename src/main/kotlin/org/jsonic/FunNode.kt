package org.jsonic

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

class FunNode(
    name: String,
    args: List<VarNode>,
    body: String,
    nodeFactory: JsonNodeFactory = ObjectMapper().nodeFactory
) : ObjectNode(
    nodeFactory,
    mapOf(
        Pair(NAME_TAG, TextNode(name)),
        Pair(ARGS_TAG, nodeFactory.arrayNode().addAll(args)),
        Pair(BODY_TAG, TextNode(body))
    )
) {

    companion object {

        const val ARGS_TAG = "args"

        const val BODY_TAG = "body"

        const val NAME_TAG = "name"

    } //~ companion

    val name get() = (this[NAME_TAG] as TextNode).textValue()

    @Suppress("UNCHECKED_CAST")
    val args get() = this[ARGS_TAG].toList() as List<VarNode>

    val body get() = (this[BODY_TAG] as TextNode).textValue()

}