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

    private val stack = ArrayDeque<ArrayNode>()

    init {
        push(ArrayNode(nf).add(root))
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

    private fun pop(): ArrayNode {
        val seq = ArrayNode(nf)
        if (stack.isNotEmpty()) {
            seq.addAll(stack.removeFirst())
        }
        return seq
    }

    private fun push(node: ArrayNode): ArrayNode {
        stack.addFirst(node)
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

    override fun visitField(ctx: JSonicParser.FieldContext): ArrayNode {
        val seq = ArrayNode(nf)
        flatten(pop()).forEach { n ->
            when (n) {
                is ObjectNode -> n[normalizeFieldName(ctx.text)]?.let { node ->
                    seq.add(node)
                }
            }
        }
        println("${ctx.text} -> $seq")
        return push(seq)
    }

    override fun visitFilter(ctx: JSonicParser.FilterContext): ArrayNode {
        val seq = ArrayNode(nf)
        visit(ctx.exp())
        val rhs = pop()
        val LHS = pop()
        LHS.forEach { n ->
            val lhs = expand(n)
            rhs.forEach { p ->
                when (p) {
                    is NumericNode -> {
                        val i = p.asInt()
                        lhs[if (i < 0) LHS.size() + i else i]?.let { seq.add(it) }
                    }

                    else -> {}
                }
            }
        }
        println("${ctx.exp().text} -> $seq")
        return push(seq)
    }

    override fun visitJsong(ctx: JSonicParser.JsongContext): JsonNode? {
        var seq: JsonNode? = null
        ctx.exp().forEach { exp ->
            seq = visit(exp)
        }
        return reduce(seq)
    }

    override fun visitMap(ctx: JSonicParser.MapContext): ArrayNode {
        val seq = ArrayNode(nf)
        val lhs = flatten(pop())
        lhs.forEach { n ->
            push(expand(n))
            visit(ctx.exp())
            seq.add(pop())
        }
        return push(seq)
    }

    override fun visitNumber(ctx: JSonicParser.NumberContext): ArrayNode {
        val seq = expand(DecimalNode(ctx.text.toBigDecimal()))
        return push(seq)
    }

}

