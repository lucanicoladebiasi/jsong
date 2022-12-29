package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.RegexNode
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
                1 -> node[0]
                else -> node
            }

            else -> node
        }
    }

    override fun visitArray(ctx: JSonicParser.ArrayContext): JsonNode? {
        val res = ArrayNode(nf)
        ctx.exp().forEach { exp ->
            visit(exp)
            pop().let { res.add(it) }
        }
        return push(res)
    }

    override fun visitBool(ctx: JSonicParser.BoolContext): JsonNode? {
        return push(
            when {
                ctx.FALSE() != null -> BooleanNode.FALSE
                ctx.TRUE() != null -> BooleanNode.TRUE
                else -> throw IllegalArgumentException("$ctx not recognized")
            }
        )
    }

    override fun visitContext(ctx: JSonicParser.ContextContext): JsonNode? {
        return stack.firstOrNull()
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
        val res = ArrayNode(nf)
        visit(ctx.lhs)
        val lhs = expand(pop())
        visit(ctx.rhs)
        when (val rhs = pop()) {
            is NumericNode -> {
                val i = rhs.asInt()
                res.add(lhs[if (i < 0) lhs.size() + i else i])
            }

            is RangesNode -> {
                rhs.indexes.forEach { index ->
                    val i = index.asInt()
                    res.add(lhs[if (i < 0) lhs.size() + i else i])
                }
            }
        }
        return push(reduce(res))
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
        expand(pop()).forEach { lhs ->
            push(lhs)
            visit(ctx.exp())
            pop()?.let { rhs ->
                when (rhs) {
                    is ArrayNode -> res.addAll(rhs)
                    else -> res.add(rhs)
                }
            }
        }
        return push(reduce(res))
    }

    override fun visitNihil(ctx: JSonicParser.NihilContext): JsonNode? {
        return push(NullNode.instance)
    }

    override fun visitNumber(ctx: JSonicParser.NumberContext): JsonNode? {
        return push(DecimalNode(ctx.text.toBigDecimal()))
    }

    override fun visitObj(ctx: JSonicParser.ObjContext): JsonNode? {
        val exp = ObjectNode(nf)
        ctx.pair().forEachIndexed { index, pair ->
            visit(pair.key)
            val key = pop()?.asText() ?: index.toString()
            visit(pair.value)
            val value = pop() ?: NullNode.instance
            exp.set<JsonNode>(key, value)
        }
        return push(exp)
    }

    override fun visitRange(ctx: JSonicParser.RangeContext): JsonNode? {
        return push(
            RangeNode.of(
                ctx.min.text.toBigDecimal(),
                ctx.max.text.toBigDecimal(),
                nf
            )
        )
    }

    override fun visitRanges(ctx: JSonicParser.RangesContext): JsonNode? {
        val res = RangesNode(nf)
        ctx.range().forEach {
            visit(it)
            res.add(pop())
        }
        return push(res)
    }

    override fun visitRegex(ctx: JSonicParser.RegexContext): JsonNode? {
        return push(RegexNode(ctx.REGEX().text))
    }

    override fun visitRoot(ctx: JSonicParser.RootContext): JsonNode? {
        return push(root)
    }

    override fun visitScope(ctx: JSonicParser.ScopeContext): JsonNode? {
        ctx.exp().forEach { exp ->
            visit(exp)
        }
        val res = pop()
        return push(reduce(res))
    }

    override fun visitText(ctx: JSonicParser.TextContext): JsonNode? {
        return push(TextNode(ctx.text.substring(1, ctx.text.length - 1)))
    }

}

