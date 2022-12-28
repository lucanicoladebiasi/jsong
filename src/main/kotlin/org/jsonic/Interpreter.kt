package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.antlr.JSonicBaseVisitor
import org.jsong.antlr.JSonicLexer
import org.jsong.antlr.JSonicParser

class Interpreter(
    private val root: JsonNode? = null,
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

    } //~ companion

    private val stack = ArrayDeque<JsonNode>()

    init {
        push(root)
    }

    fun evaluate(exp: String): JsonNode? {
        return visit(JSonicParser(CommonTokenStream(JSonicLexer(CharStreams.fromString(exp)))).jsong())
    }

    private fun expand(node: JsonNode?): ArrayNode {
        return when (node) {
            null -> ArrayNode(nf)
            is ArrayNode -> node
            else -> ArrayNode(nf).add(node)
        }
    }

    private fun flatten(node: ArrayNode): ArrayNode {
        val seq = ArrayNode(nf)
        node.forEach { n ->
            when (n) {
                is ArrayNode -> seq.addAll(n)
                else -> seq.add(n)
            }
        }
        return seq
    }

    private fun pop(): JsonNode? {
        return stack.removeFirstOrNull()
    }

    private fun push(node: JsonNode?): JsonNode? {
        if (node != null) {
            stack.addFirst(node)
        }
        return node
    }

    private fun reduce(node: JsonNode?): JsonNode? {
        return when (node) {
            is ArrayNode -> when (node.size()) {
                0 -> null
                1 -> reduce(node[0])
                else -> ArrayNode(nf).addAll(node.map { reduce(it) })
            }

            else -> node
        }
    }

    override fun visitField(ctx: JSonicParser.FieldContext): JsonNode? {
        return push(
            when (val lhs = pop()) {
                is ObjectNode -> lhs[normalizeFieldName(ctx.text)]
                else -> null
            }
        )
    }

    override fun visitFilter(ctx: JSonicParser.FilterContext): JsonNode? {
        val seq = ArrayNode(nf)
        visit(ctx.lhs)
        val lhs = expand(pop())
        visit(ctx.rhs)
        when(val rhs = pop()) {
            is NumericNode -> {
                seq.add(lhs[rhs.asInt()])
            }
        }
        return push(seq)
    }

    override fun visitJsong(ctx: JSonicParser.JsongContext): JsonNode? {
        var seq: JsonNode? = null
        ctx.exp().forEach { exp ->
            seq = visit(exp)
        }
        return seq
    }

    override fun visitMap(ctx: JSonicParser.MapContext): JsonNode? {
        val seq = ArrayNode(nf)
        expand(pop()).forEach { lhs ->
            push(lhs)
            visit(ctx.exp())
            pop()?.let { rhs ->
                seq.add(rhs)
            }
        }
        return push(seq)
    }

    override fun visitNumber(ctx: JSonicParser.NumberContext): JsonNode? {
        return push(DecimalNode(ctx.text.toBigDecimal()))
    }

    override fun visitScope(ctx: JSonicParser.ScopeContext): JsonNode? {
        var seq: JsonNode? = null
        ctx.exp().forEach { exp ->
            visit(exp)
            seq = pop()
        }
        return push(seq)
    }

}

