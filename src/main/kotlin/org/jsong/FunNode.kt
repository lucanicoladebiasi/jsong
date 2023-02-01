package org.jsong

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

class FunNode(
    args: List<String>,
    body: String,
    nodeFactory: JsonNodeFactory = ObjectMapper().nodeFactory
) : ObjectNode(
    nodeFactory,
    mapOf(
        Pair(ARGS_TAG, nodeFactory.arrayNode().addAll(args.map { arg -> TextNode(arg) })),
        Pair(BODY_TAG, TextNode(body))
    )
) {

    companion object {

        const val ARGS_TAG = "args"

        const val BODY_TAG = "body"

    } //~ companion

    val args get() = this[ARGS_TAG].map { arg -> arg.textValue() }

    val body get() = (this[BODY_TAG] as TextNode).textValue()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as FunNode

        if (args != other.args) return false
        if (body != other.body) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + args.hashCode()
        result = 31 * result + (body?.hashCode() ?: 0)
        return result
    }


}