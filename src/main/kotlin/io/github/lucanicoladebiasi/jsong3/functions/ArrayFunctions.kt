package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.DecimalNode
import io.github.lucanicoladebiasi.jsong3.Visitor
import io.github.lucanicoladebiasi.jsong3.Visitor.Companion.expand
import kotlin.random.Random

@Suppress("unused")
class ArrayFunctions(
    private val om: ObjectMapper,
    private val rand: Random
) {

    /**
     * https://docs.jsonata.org/array-functions#count
     */
    @LibraryFunction
    fun count(node: JsonNode): DecimalNode {
        return DecimalNode(expand(node, om).count().toBigDecimal())
    }

    /**
     * https://docs.jsonata.org/array-functions#append
     */
    @LibraryFunction
    fun append(node1: JsonNode, node2: JsonNode): ArrayNode {
        val result = om.createArrayNode()
        result.addAll(expand(node1, om))
        result.addAll(expand(node2, om))
        return result
    }

    /**
     * https://docs.jsonata.org/array-functions#sort
     */
    @LibraryFunction
    fun sort(node: JsonNode): ArrayNode {
        val result = om.createArrayNode()
        result.addAll(
            expand(node, om).sortedWith { lhs, rhs ->
                Visitor.compare(lhs, rhs, om.writer())
            }
        )
        return result
    }

    /**
     * https://docs.jsonata.org/array-functions#sort
     */
    @LibraryFunction
    fun sort(node: JsonNode, function: JsonNode) {
        TODO()
    }

    /**
     * https://docs.jsonata.org/array-functions#reverse
     */
    @LibraryFunction
    fun reverse(node: JsonNode): ArrayNode {
        val result = om.createArrayNode()
        result.addAll(expand(node, om).reversed())
        return result
    }

    /**
     * https://docs.jsonata.org/array-functions#shuffle
     */
    @LibraryFunction
    fun shuffle(node: JsonNode): ArrayNode {
        val result = om.createArrayNode()
        result.addAll(expand(node, om).shuffled(rand))
        return result
    }

    /**
     * https://docs.jsonata.org/array-functions#distinct
     */
    @LibraryFunction
    fun distinct(node: JsonNode): ArrayNode {
        val result = om.createArrayNode()
        result.addAll(expand(node, om).toSet())
        return result
    }

    /**
     * https://docs.jsonata.org/array-functions#zip
     */
    @LibraryFunction
    fun zip(vararg arrays: ArrayNode): ArrayNode {
        val result = om.createArrayNode()
        val size = arrays.minOf { array -> array.size() }
        repeat(size) { i ->
            result.add(om.createArrayNode())
        }
        repeat(size) { i ->
            (result[i] as ArrayNode).add(arrays[i][i])
        }
        return result
    }

} //~ ArrayFunctions