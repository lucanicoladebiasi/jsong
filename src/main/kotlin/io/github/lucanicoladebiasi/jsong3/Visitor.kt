package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
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

    private fun bindPositionalVariable(ctx: JSong3Parser.PvbContext?, value: ArrayNode) {
        if (ctx != null) {
            val id = sanitise(ctx.`var`().ID().text)
            context.variables[id] = BindPositionNode(context.mapper).addAll(value)
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

    override fun visitCallContext(ctx: JSong3Parser.CallContextContext): JsonNode? {
        return context.node
    }

    override fun visitCallVariable(ctx: JSong3Parser.CallVariableContext): JsonNode? {
        val id = sanitise(ctx.`var`().ID().text)
        return when (val result = context.variables[id]) {
            is BindContextNode -> when (context.loop != null) {
                true -> result.get(context.loop)
                else -> result
            }

            is BindPositionNode -> when (context.loop != null) {
                true -> result.get(context.node)
                else -> result
            }

            else -> result
        }
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

    override fun visitEvalCompare(ctx: JSong3Parser.EvalCompareContext): BooleanNode {
        val lhs = reduce(Visitor(context).visit(ctx.lhs))
        val rhs = reduce(Visitor(context).visit(ctx.rhs))
        return BooleanNode.valueOf(
            when {
                lhs == null && rhs == null -> true
                lhs != null && rhs != null -> {
                    when (val op = ctx.op.type) {
                        JSong3Parser.IN -> expand(rhs).contains(lhs)
                        else -> {
                            val value = compare(lhs, rhs)
                            when (op) {
                                JSong3Parser.GE -> value >= 0
                                JSong3Parser.GT -> value > 0
                                JSong3Parser.EQ -> value == 0
                                JSong3Parser.NE -> value != 0
                                JSong3Parser.LE -> value <= 0
                                JSong3Parser.LT -> value < 0
                                else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
                            }
                        }
                    }
                }

                else -> false
            }
        )
    }

    override fun visitEvalConcatenate(ctx: JSong3Parser.EvalConcatenateContext): TextNode {
        val writer = context.mapper.writer()
        val lhs = stringOf(reduce(Visitor(context).visit(ctx.lhs)), writer)
        val rhs = stringOf(reduce(Visitor(context).visit(ctx.rhs)), writer)
        return TextNode(lhs.plus(rhs))
    }

    @Throws(UnsupportedOperationException::class)
    override fun visitEvalDivModMul(ctx: JSong3Parser.EvalDivModMulContext): DecimalNode {
        val lhs = decimalOf(reduce(Visitor(context).visit(ctx.lhs)))
        val rhs = decimalOf(reduce(Visitor(context).visit(ctx.rhs)))
        return DecimalNode(
            when (ctx.op.type) {
                JSong3Parser.DIV -> lhs.divide(rhs, context.mathContext)
                JSong3Parser.MOD -> lhs.remainder(rhs)
                JSong3Parser.MUL -> lhs.multiply(rhs)
                else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
            }
        )
    }

    @Throws(IllegalArgumentException::class)
    override fun visitEvalNegative(ctx: JSong3Parser.EvalNegativeContext): DecimalNode {
        return DecimalNode(decimalOf(reduce(Visitor(context).visit(ctx.exp()))).negate())
    }

    @Throws(UnsupportedOperationException::class)
    override fun visitEvalSumSub(ctx: JSong3Parser.EvalSumSubContext): DecimalNode {
        val lhs = decimalOf(reduce(Visitor(context).visit(ctx.lhs)))
        val rhs = decimalOf(reduce(Visitor(context).visit(ctx.rhs)))
        return DecimalNode(
            when (ctx.op.type) {
                JSong3Parser.SUB -> lhs.subtract(rhs)
                JSong3Parser.SUM -> lhs.add(rhs)
                else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
            }
        )
    }

    override fun visitFalse(ctx: JSong3Parser.FalseContext): BooleanNode {
        return BooleanNode.FALSE
    }

    override fun visitFilter(ctx: JSong3Parser.FilterContext): ArrayNode {
        val result = context.createArrayNode()
        val lhs = expand(Visitor(context).visit(ctx.lhs))
        bindPositionalVariable(ctx.pvb(), lhs)
        val loop = Context.Loop(lhs.size())
        lhs.forEachIndexed { index, node ->
            Visitor(Context(node, loop.at(index), context)).visit(ctx.rhs)?.let { rhs ->
                when (rhs) {
                    is ArrayNode -> {
                        val indexes = RangeNode.indexes(rhs)
                        when (indexes.isNotEmpty()) {
                            true -> if (indexes.contains(index)) result.add(node)
                            else -> if (booleanOf(rhs)) result.add(node)
                        }
                    }

                    is NumericNode -> {
                        val value = rhs.asInt()
                        val offset = if (value < 0) loop.size + value else value
                        if (index == offset) result.add(node)
                    }

                    else -> if (booleanOf(rhs)) result.add(node)
                }
            }
        }
        return result
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
        val lhs = expand(Visitor(context).visit(ctx.lhs))
        val loop = Context.Loop(lhs.size())
        lhs.forEachIndexed { index, node ->
            Visitor(Context(node, loop.at(index), context)).visit(ctx.rhs)?.let { rhs ->
                if (rhs is ArrayNode) result.addAll(rhs) else result.add(rhs)
            }
        }
        bindPositionalVariable(ctx.pvb(), result)
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
            val value = reduce(Visitor(context).visit(fieldCtx.`val`))
            result.set<JsonNode>(key, value)
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