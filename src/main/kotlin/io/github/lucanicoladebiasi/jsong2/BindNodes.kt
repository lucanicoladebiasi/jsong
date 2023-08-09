package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

open class ContextualNode(nf: JsonNodeFactory) : ArrayNode(nf) {

    open class Bind(nf: JsonNodeFactory, context: JsonNode, value: JsonNode) : ObjectNode(
        nf, mapOf(
            Pair(CTX_TAG, context), Pair(
                VAL_TAG, value
            )
        )
    ) {

        companion object {

            const val CTX_TAG = "ctx"

            const val VAL_TAG = "val"

        } //~ companion

        val context = this[CTX_TAG]

        open val value = this[VAL_TAG]

    } //~ Bind

} //~ BindNode

class PositionalNode(nf: JsonNodeFactory) : ContextualNode(nf) {

    class Bind(nf: JsonNodeFactory, context: JsonNode, value: IntNode) : ContextualNode.Bind(nf, context, value) {

        override val value = get(VAL_TAG) as IntNode

    } //~ Bind

    fun position(context: JsonNode?): IntNode? {
        if (context != null) forEach { node ->
            if (node is Bind && node.context == context) {
                return node.value
            }
        }
        return null
    }

} //~ PositionalNode
