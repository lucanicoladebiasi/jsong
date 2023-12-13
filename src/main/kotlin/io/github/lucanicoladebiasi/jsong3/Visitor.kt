package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import io.github.lucanicoladebiasi.jsong.antlr.JSong3BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Parser
import io.github.lucanicoladebiasi.jsong3.functions.BooleanFunctions.Companion.booleanOf
import io.github.lucanicoladebiasi.jsong3.functions.NumericFunctions.Companion.decimalOf
import io.github.lucanicoladebiasi.jsong3.functions.StringFunctions.Companion.stringOf
import org.apache.commons.text.StringEscapeUtils

class Visitor(
    private val context: Context
) : JSong3BaseVisitor<JsonNode?>() {

    companion object {

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

        private fun sanitise(txt: String): String {
            return StringEscapeUtils.unescapeJson(
                when {
                    txt.startsWith("`") && txt.endsWith("`")
                            || txt.startsWith("'") && txt.endsWith("'")
                            || txt.startsWith("\"") && txt.endsWith("\"") -> {
                        txt.substring(1, txt.length - 1)
                    }

                    else -> txt
                }
            )
        }

    } //~ companion

    private fun compare(lhs: JsonNode, rhs: JsonNode): Int {
        val writer = context.mapper.writer()
        return when {
            lhs.isNumber && rhs.isNumber -> lhs.decimalValue().compareTo(rhs.decimalValue())
            else -> stringOf(lhs, writer).compareTo(stringOf(rhs, writer))
        }
    }

    private fun expand(node: JsonNode?): ArrayNode {
        val result = context.createArrayNode()
        if (node != null) when (node) {
            is ArrayNode -> result.addAll(node)
            else -> result.add(node)
        }
        return result
    }

    override fun visitArray(ctx: JSong3Parser.ArrayContext): ArrayNode {
        val result = context.createArrayNode()
        ctx.element().forEach { ctxElement ->
            Visitor(context).visit(ctxElement)?.let { element ->
                result.add(element)
            }
        }
        return result
    }

    override fun visitBlock(ctx: JSong3Parser.BlockContext): JsonNode? {
        var exp = context.node
        ctx.exp()?.forEach { ctxExp ->
            exp = Visitor(Context(exp, null, context)).visit(ctxExp)
        }
        return reduce(exp)
    }

    override fun visitCompare(ctx: JSong3Parser.CompareContext): BooleanNode {
        val lhs = reduce(Visitor(context).visit(ctx.lhs))
        val rhs = reduce(Visitor(context).visit(ctx.rhs))
        return if (lhs != null && rhs != null) {
            val value = compare(lhs, rhs)
            BooleanNode.valueOf(
                when (ctx.op.type) {
                    JSong2Parser.LT -> value < 0
                    JSong2Parser.LE -> value <= 0
                    JSong2Parser.GE -> value >= 0
                    JSong2Parser.GT -> value > 0
                    JSong2Parser.NE -> value != 0
                    JSong2Parser.EQ -> value == 0
                    else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
                }
            )
        } else BooleanNode.FALSE
    }

    override fun visitConcatenate(ctx: JSong3Parser.ConcatenateContext): TextNode {
        val writer = context.mapper.writer()
        val lhs = stringOf(reduce(Visitor(context).visit(ctx.lhs)), writer)
        val rhs = stringOf(reduce(Visitor(context).visit(ctx.rhs)), writer)
        return TextNode(lhs.plus(rhs))
    }

    override fun visitContext(ctx: JSong3Parser.ContextContext): ArrayNode {
        return expand(context.node)
    }

    override fun visitEvalAndOr(ctx: JSong3Parser.EvalAndOrContext): BooleanNode {
        val lhs = booleanOf(reduce(Visitor(context).visit(ctx.lhs)))
        val rhs = booleanOf(reduce(Visitor(context).visit(ctx.rhs)))
        return BooleanNode.valueOf(
            when (ctx.op.type) {
                JSong3Parser.AND -> lhs && rhs
                JSong3Parser.OR -> lhs || rhs
                else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
            }
        )
    }

    @Throws(UnsupportedOperationException::class)
    override fun visitEvalMulDivMod(ctx: JSong3Parser.EvalMulDivModContext): DecimalNode {
        val lhs = decimalOf(reduce(Visitor(context).visit(ctx.lhs)))
        val rhs = decimalOf(reduce(Visitor(context).visit(ctx.rhs)))
        return DecimalNode(
            when (ctx.op.type) {
                JSong3Parser.PERCENT -> lhs.remainder(rhs)
                JSong3Parser.SLASH -> lhs.divide(rhs, context.mathContext)
                JSong3Parser.STAR -> lhs.multiply(rhs)
                else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
            }
        )
    }

    @Throws(IllegalArgumentException::class)
    override fun visitEvalNegate(ctx: JSong3Parser.EvalNegateContext): DecimalNode {
        return DecimalNode(decimalOf(reduce(Visitor(context).visit(ctx.exp()))).negate())
    }

    @Throws(UnsupportedOperationException::class)
    override fun visitEvalSumSub(ctx: JSong3Parser.EvalSumSubContext): DecimalNode {
        val lhs = decimalOf(reduce(Visitor(context).visit(ctx.lhs)))
        val rhs = decimalOf(reduce(Visitor(context).visit(ctx.rhs)))
        return DecimalNode(
            when (ctx.op.type) {
                JSong3Parser.DASH -> lhs.subtract(rhs)
                JSong3Parser.PLUS -> lhs.add(rhs)
                else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
            }
        )
    }

    override fun visitFalse(ctx: JSong3Parser.FalseContext): BooleanNode {
        return BooleanNode.FALSE
    }

    override fun visitId(ctx: JSong3Parser.IdContext): JsonNode? {
        val fieldName = sanitise(ctx.ID().text)
        return if (context.node is ObjectNode && context.node.has(fieldName)) {
            context.node[fieldName]
        } else
            null
    }

    override fun visitJsong(ctx: JSong3Parser.JsongContext): JsonNode? {
        var exp = context.node
        ctx.exp()?.forEach { ctxExp ->
            exp = Visitor(Context(exp, null, context)).visit(ctxExp)
        }
        return reduce(exp)
    }

    override fun visitMap(ctx: JSong3Parser.MapContext): ArrayNode {
        val result = context.createArrayNode()
        expand(context.node).forEachIndexed { index, node ->
            Visitor(Context(node, index, context)).visit(ctx.exp())?.let { exp ->
                when (val ctxPredicate = ctx.predicate()) {
                    null -> result.add(exp)
                    else -> reduce(Visitor(Context(exp, index, context)).visit(ctxPredicate))?.let { element ->
                        result.add(element)
                    }
                }
            }
        }
        return result
    }

    override fun visitNull(ctx: JSong3Parser.NullContext?): NullNode {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSong3Parser.NumberContext): DecimalNode {
        return DecimalNode(ctx.NUMBER().text.toBigDecimal())
    }

    override fun visitObject(ctx: JSong3Parser.ObjectContext): ObjectNode {
        val result = context.createObjectNode()
        ctx.field().forEachIndexed { index, fieldCtx ->
            val key = reduce(Visitor(context).visit(fieldCtx.key))?.asText() ?: index.toString()
            val value = Visitor(context).visit(fieldCtx.`val`)
            result.set<JsonNode>(key, value)
        }
        return result
    }

    override fun visitPredicate(ctx: JSong3Parser.PredicateContext): ArrayNode {
        val result = context.createArrayNode()
        val sequence = expand(context.node)
        val size = sequence.size()
        sequence.forEachIndexed { index, node ->
            Visitor(Context(node, index, context)).visit(ctx.exp())?.let { predicate ->
                when (predicate) {
                    is ArrayNode -> {
                        val indexes = RangeNode.indexes(predicate)
                        when(indexes.isNotEmpty()) {
                            true -> if (indexes.contains(index)) result.add(node)
                            else -> TODO()
                        }
                    }
                    is NumericNode -> {
                        val position = predicate.asInt()
                        val offset = if (position < 0) size + position else position
                        if (index == offset) result.add(node)
                    }
                    else -> TODO()
                }
            }
        }
        return result
    }

    override fun visitRange(ctx: JSong3Parser.RangeContext): RangeNode {
        val lhs = decimalOf(reduce(Visitor(context).visit(ctx.lhs)))
        val rhs = decimalOf(reduce(Visitor(context).visit(ctx.rhs)))
        return RangeNode.between(lhs, rhs, context.mapper)
    }

    override fun visitRegex(ctx: JSong3Parser.RegexContext): RegexNode {
        val pattern = ctx.pattern().text
        return when {
            ctx.REG_CI() != null -> RegexNode.ci(pattern)
            ctx.REG_ML() != null -> RegexNode.ml(pattern)
            else -> RegexNode.of(pattern)
        }
    }

    override fun visitText(ctx: JSong3Parser.TextContext): TextNode {
        return TextNode(sanitise(ctx.STRING().text))
    }

    override fun visitTrue(ctx: JSong3Parser.TrueContext?): BooleanNode {
        return BooleanNode.TRUE
    }

} //~ Visitor