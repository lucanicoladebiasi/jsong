package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.antlr.JSonicBaseVisitor
import org.jsong.antlr.JSonicLexer
import org.jsong.antlr.JSonicParser

class Interpreter(
    private val root: JsonNode?= null,
    private val nf: JsonNodeFactory = ObjectMapper().nodeFactory
) : JSonicBaseVisitor<JsonNode?>() {

    companion object {

        private const val BACKTICK = '`'

        private fun normalizeFieldName(tag: String): String {
            return when {
                tag[0] == BACKTICK && tag[tag.length - 1] == BACKTICK -> tag.substring(1, tag.length - 1)
                else -> tag
            }
        }

    }

    private val stack = ArrayDeque<JsonNode>()

    init {
        push(root)
    }

    fun evaluate(exp: String): JsonNode? {
        return visit(JSonicParser(CommonTokenStream(JSonicLexer(CharStreams.fromString(exp)))).jsong())
    }

    private fun pop(): SequenceNode {
        val seq = SequenceNode(nf)
        if (stack.isNotEmpty()) {
            seq.add(stack.removeFirst())
        }
        return seq
    }

    private fun push(node: JsonNode?): JsonNode? {
        return when (node) {
            is ArrayNode -> when (node.size()) {
                0 -> {
                    stack.addFirst(ArrayNode(nf))
                    null
                }
                1 -> {
                    stack.addFirst(node[0])
                    node[0]
                }
                else -> {
                    stack.addFirst(node)
                    node
                }
            }
            null -> {
                stack.addFirst(ArrayNode(nf))
                null
            }
            else -> {
                stack.addFirst(node)
                node
            }
        }
    }


    override fun visitField(ctx: JSonicParser.FieldContext): JsonNode? {
        val res = ArrayNode(nf)
        pop().forEach { e ->
            when (e) {
                is ObjectNode -> e[normalizeFieldName(ctx.text)]?.let { node ->
                    res.add(node)
                }
            }
        }
        return push(res)
    }

    override fun visitJsong(ctx: JSonicParser.JsongContext): JsonNode? {
        var res: JsonNode? = null
        ctx.exp().forEach { exp ->
            res = visit(exp)
        }
        return res
    }

    override fun visitMap(ctx: JSonicParser.MapContext): JsonNode? {
        val res = ArrayNode(nf)
        pop().forEach { e ->
            push(e)
            visit(ctx.exp())
            res.addAll(pop())
        }
        return push(res)
    }

}