package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

abstract class Function(
    mapper: ObjectMapper,
    name: String,
    args: Map<String, JsonNode?>
): ObjectNode(
    mapper.nodeFactory,
    mapOf<String, JsonNode>(Pair(NAME_TAG, TextNode(name)), Pair(ARGS_TAG, ObjectNode(mapper.nodeFactory, args)))
) {

    companion object {

        const val ARGS_TAG = "args"

        const val NAME_TAG = "name"

    } //~ companion


} //~ Function