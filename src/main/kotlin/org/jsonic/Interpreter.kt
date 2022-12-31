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

    private var context: JsonNode? = null

    init {
        context = root
    }

    private fun context(node: JsonNode?): JsonNode? {
        context = node
        return context
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
            res.add(visit(exp))
        }
        return context(res)
    }

    override fun visitBool(ctx: JSonicParser.BoolContext): JsonNode? {
        return context(
            when {
                ctx.FALSE() != null -> BooleanNode.FALSE
                ctx.TRUE() != null -> BooleanNode.TRUE
                else -> throw IllegalArgumentException("$ctx not recognized")
            }
        )
    }

    override fun visitContext(ctx: JSonicParser.ContextContext): JsonNode? {
        return context
    }

    override fun visitEq(ctx: JSonicParser.EqContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(lhs == rhs)
        return res
    }

    override fun visitField(ctx: JSonicParser.FieldContext): JsonNode? {
        val res = when (context) {
            is ObjectNode -> {
                val node = context as ObjectNode
                val field = normalizeFieldName(ctx.text)
                when (node.has(field)) {
                    true -> node[field]
                    else -> null
                }
            }

            else -> null
        }
        return context(res)
    }

    override fun visitFilter(ctx: JSonicParser.FilterContext): JsonNode? {
        val res = ArrayNode(nf)
        val lhs = expand(visit(ctx.lhs))
        lhs.forEachIndexed { index, node ->
            context(lhs)
            when (val rhs = visit(ctx.rhs)) {
                is NumericNode -> {
                    val value = rhs.asInt()
                    val offset = if (value < 0) lhs.size() + value else value
                    if (index == offset) {
                        res.add(node)
                    }
                }

                is RangesNode -> {
                    if (rhs.indexes.map { it.asInt() }.contains(index)) {
                        res.add(node)
                    }
                }

                else -> {
                    val predicate = rhs?.asBoolean() ?: false
                    if (predicate) {
                        res.add(node)
                    }
                }

            }
        }
        return reduce(res)
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
        expand(visit(ctx.lhs)).forEach { lhs ->
            context(lhs)
            when (val rhs = visit(ctx.rhs)) {
                is ArrayNode -> res.addAll(rhs)
                else -> rhs?.let { res.add(it) }
            }
        }
        return context(reduce(res))
    }

    override fun visitNihil(ctx: JSonicParser.NihilContext): JsonNode? {
        return context(NullNode.instance)
    }

    override fun visitNumber(ctx: JSonicParser.NumberContext): JsonNode? {
        return context(DecimalNode(ctx.text.toBigDecimal()))
    }

    override fun visitObj(ctx: JSonicParser.ObjContext): JsonNode? {
        val res = ObjectNode(nf)
        ctx.pair().forEachIndexed { index, pair ->
            val key = visit(pair.key)?.asText() ?: index.toString()
            val value = visit(pair.value) ?: NullNode.instance
            res.set<JsonNode>(key, value)
        }
        return context(res)
    }

    override fun visitRange(ctx: JSonicParser.RangeContext): JsonNode? {
        return context(
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
            res.add(visit(it))
        }
        return context(res)
    }

    override fun visitRegex(ctx: JSonicParser.RegexContext): JsonNode? {
        return context(RegexNode(ctx.REGEX().text))
    }

    override fun visitRoot(ctx: JSonicParser.RootContext): JsonNode? {
        return context(root)
    }

    override fun visitScope(ctx: JSonicParser.ScopeContext): JsonNode? {
        ctx.exp().forEach { exp ->
            context(visit(exp))
        }
        return context
    }

    override fun visitText(ctx: JSonicParser.TextContext): JsonNode? {
        return context(TextNode(ctx.text.substring(1, ctx.text.length - 1)))
    }

}

