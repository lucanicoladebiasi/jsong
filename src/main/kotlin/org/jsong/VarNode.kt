package org.jsong

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

open class VarNode(
    name: String,
    value: ArrayNode = ObjectMapper().createArrayNode(),
    private val nodeFactory: JsonNodeFactory = ObjectMapper().nodeFactory
): ObjectNode(nodeFactory, mapOf(Pair(NAME_TAG, TextNode(name)), Pair(VALUE_TAG, value))) {

    companion object {

        const val NAME_TAG = "name"

        const val VALUE_TAG = "value"

    } //~ companion

    val name get() = this[NAME_TAG] as TextNode

    val value get() = this[VALUE_TAG] as ArrayNode

    fun stretch(size: Int): VarNode {
        val value =  nodeFactory.arrayNode()
        val multi = size / this.value.size()
        this.value.forEach { element ->
            for (i in 0 until multi) {
                value.add(element)
            }
        }
        this.set<ArrayNode>(VALUE_TAG, value)
        return this
    }

} //~ VarNode

class PositionalVarNode(
    name: String,
    value: ArrayNode = ObjectMapper().createArrayNode(),
    nodeFactory: JsonNodeFactory = ObjectMapper().nodeFactory
): VarNode(name, value, nodeFactory)