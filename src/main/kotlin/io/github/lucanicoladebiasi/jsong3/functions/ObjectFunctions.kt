package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong3.FunctionNode
import io.github.lucanicoladebiasi.jsong3.RegexNode

@Suppress("unused")
class ObjectFunctions(val om: ObjectMapper) {

    enum class Type {
        array,
        boolean,
        function,
        `null`,
        number,
        `object`,
        regex,
        string,
        undefined
    }

    companion object {

        fun typeOf(node: JsonNode?): Type {
            return when(node) {
                null -> Type.`null`
                is ArrayNode -> Type.array
                is BooleanNode -> Type.boolean
                is FunctionNode -> Type.function
                is NumericNode -> Type.number
                is ObjectNode -> Type.`object`
                is RegexNode -> Type.regex
                is TextNode -> Type.string
                else -> Type.undefined
            }
        }

    } //~ companion

    /**
     * https://docs.jsonata.org/object-functions#keys
     */
    @LibraryFunction
    fun keys(node: JsonNode?): ArrayNode {
        val keys = mutableSetOf<String>()
        when (node) {
            is ArrayNode -> node.filterIsInstance<ObjectNode>().forEach { element ->
                element.fieldNames().forEach { key ->
                    if (!keys.contains(key)) keys.add(key)
                }
            }

            is ObjectNode -> node.fieldNames().forEach { key ->
                if (!keys.contains(key)) keys.add(key)
            }
        }
        return om.createArrayNode().addAll(keys.map { key -> TextNode(key) })
    }

    /**
     * https://docs.jsonata.org/object-functions#lookup
     */
    @LibraryFunction
    fun lookup(node: JsonNode?, key: TextNode): ArrayNode {
        val result = om.createArrayNode()
        when (node) {
            is ArrayNode -> node.filterIsInstance<ObjectNode>().forEach { element ->
                element[key.textValue()]?.let { value ->
                    result.add(value)
                }
            }

            is ObjectNode -> node[key.textValue()]?.let { value ->
                result.add(value)
            }
        }
        return result
    }

    /**
     * https://docs.jsonata.org/object-functions#spread
     */
    @LibraryFunction
    fun spread(node: JsonNode): ArrayNode {
        val result = om.createArrayNode()
        when (node) {
            is ArrayNode -> node.filterIsInstance<ObjectNode>().forEach { element ->
                element.fields().forEach { field ->
                    result.add(om.createObjectNode().set<ObjectNode>(field.key, field.value))
                }
            }

            is ObjectNode -> node.fields().forEach { field ->
                result.add(om.createObjectNode().set<ObjectNode>(field.key, field.value))
            }
        }
        return result
    }

    /**
     * https://docs.jsonata.org/object-functions#merge
     */
    @LibraryFunction
    fun merge(array: ArrayNode): ObjectNode {
        val result = om.createObjectNode()
        array.filterIsInstance<ObjectNode>().forEach { element ->
            element.fields().forEach { field ->
                result.set<ObjectNode>(field.key, field.value)
            }
        }
        return result
    }

    /**
     * https://docs.jsonata.org/object-functions#sift
     */
    @LibraryFunction
    fun sift(node: ObjectNode, function: ObjectNode) {
        TODO()
    }

    /**
     * https://docs.jsonata.org/object-functions#each
     */
    @LibraryFunction
    fun each(node: ObjectNode, function: ObjectNode) {
        TODO()
    }

    /**
     * https://docs.jsonata.org/object-functions#error
     */
    @LibraryFunction
    @Throws(Error::class)
    fun error(message: TextNode?) {
        throw Error(message?.textValue())
    }

    /**
     * https://docs.jsonata.org/object-functions#assert
     */
    @Throws(AssertionError::class)
    @LibraryFunction
    fun assert(condition: BooleanNode, message: TextNode): BooleanNode {
        if (!condition.booleanValue()) {
            throw AssertionError(message)
        }
        return condition
    }

    /**
     * https://docs.jsonata.org/object-functions#type
     */
    @LibraryFunction
    fun type(value: JsonNode?): TextNode {
        return TextNode(typeOf(value).name)
    }

} //~ ObjectFunctions