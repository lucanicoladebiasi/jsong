package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

class FunctionNode(
    args: List<String>,
    body: String,
    om: ObjectMapper
) : ObjectNode(
    om.nodeFactory,
    mapOf(
        Pair(ARGS_TAG, om.createArrayNode().addAll(args.map { arg -> TextNode(arg) })),
        Pair(BODY_TAG, TextNode(body))
    )
) {

    companion object {

        const val ARGS_TAG = "args"

        const val BODY_TAG = "body"

    } //~ companion

    /**
     * @property args arguments' set of this function.
     */
    val args get() = this[ARGS_TAG].map { arg -> arg.textValue() }

    /**
     * @property body code of the function.
     */
    val body get() = (this[BODY_TAG] as TextNode).textValue()


} //~ FunctionNode

