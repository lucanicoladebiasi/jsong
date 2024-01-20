package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.DecimalNode
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Lexer
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Parser
import io.github.lucanicoladebiasi.jsong3.Context
import io.github.lucanicoladebiasi.jsong3.FunctionNode
import io.github.lucanicoladebiasi.jsong3.Visitor
import io.github.lucanicoladebiasi.jsong3.Visitor.Companion.expand
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.math.MathContext
import kotlin.random.Random

@Suppress("unused")
class ArrayFunctions(
    private val lib: Library,
    private val mc: MathContext,
    private val om: ObjectMapper,
    private val rand: Random,
    private val vars: MutableMap<String, JsonNode?>
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
    fun sort(node: JsonNode, func: FunctionNode): JsonNode? {
        val result = om.createArrayNode()
        result.addAll(
            expand(node, om).sortedWith { lhs, rhs ->
                val vars = mutableMapOf<String, JsonNode?>()
                vars[func.args[0]] = lhs
                vars[func.args[1]] = rhs
                val parser = JSong3Parser(CommonTokenStream(JSong3Lexer(CharStreams.fromString(func.body))))
                Visitor(Context(lib, null, mc, null, om, mutableMapOf(), rand, vars))
                    .visit(parser.jsong())?.let {predicate ->
                        when(predicate.booleanValue()) {
                            true -> 1
                            else -> -1
                        }
                }?: 0
            })
        return result
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