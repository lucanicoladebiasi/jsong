package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong3BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Parser
import io.github.lucanicoladebiasi.jsong3.functions.BooleanFunctions.Companion.booleanOf
import io.github.lucanicoladebiasi.jsong3.functions.NumericFunctions.decimalOf
import io.github.lucanicoladebiasi.jsong3.functions.StringFunctions.Companion.stringOf
import org.antlr.v4.runtime.tree.ParseTree
import org.apache.commons.text.StringEscapeUtils

class Visitor(
    private val c: Context
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
        val writer = c.om.writer()
        return when {
            lhs.isNumber && rhs.isNumber -> lhs.decimalValue().compareTo(rhs.decimalValue())
            else -> stringOf(lhs, writer).compareTo(stringOf(rhs, writer))
        }
    }

    private fun bindContextualValue(
        ctx: JSong3Parser.CvbContext?,
        value: ArrayNode,
        carryForward: ArrayNode
    ): ArrayNode {
        return if (ctx != null) {
            val id = sanitise(ctx.`var`().ID().text)
            c.vars[id] = c.createBindContextNode().addAll(value)
            val result = c.createArrayNode()
            repeat(value.size() / carryForward.size()) {
                result.addAll(carryForward)
            }
            result
        } else value
    }

    private fun bindPositionalVariable(ctx: JSong3Parser.PvbContext?, value: ArrayNode) {
        if (ctx != null) {
            val id = sanitise(ctx.`var`().ID().text)
            val bpn = c.createBindPositionNode()
            value.forEachIndexed { index, element ->
                bpn.add(index, element)
            }
            c.vars[id] = bpn
        }
    }

    private fun descendants(node: JsonNode?): ArrayNode {
        val result = c.createArrayNode()
        result.add(node)
        node?.fields()?.forEach { field ->
            if (field.value != null) {
                result.addAll(descendants(field.value))
            }
        }
        return result
    }

    private fun expand(node: JsonNode?): ArrayNode {
        val result = c.createArrayNode()
        if (node != null) when (node) {
            is ArrayNode -> result.addAll(node)
            else -> result.add(node)
        }
        return result
    }

    private fun filter(array: ArrayNode, predicates: BooleanArray): ArrayNode {
        val result = when (array) {
            is BindContextNode -> c.createBindContextNode()
            is BindPositionNode -> c.createBindPositionNode()
            else -> c.createArrayNode()
        }
        for (i in 0 until minOf(array.size(), predicates.size)) {
            if (predicates[i]) result.add(array[i])
        }
        return result
    }

    private fun filter(
        variables: MutableMap<String, JsonNode>,
        predicates: BooleanArray
    ): MutableMap<String, JsonNode> {
        val result = mutableMapOf<String, JsonNode>()
        variables.forEach { (id, value) ->
            when (value) {
                is BindContextNode -> result[id] = filter(value, predicates)
                is BindPositionNode -> result[id] = filter(value, predicates)
                else -> result[id] = value
            }
        }
        return result
    }

    private fun map(
        lpt: ParseTree,
        rpt: ParseTree,
        pvb: JSong3Parser.PvbContext? = null,
        cvb: JSong3Parser.CvbContext? = null
    ): ArrayNode {
        val result = c.createArrayNode()
        val lhs = expand(Visitor(c).visit(lpt))
        lhs.forEachIndexed { index, node ->
            val loop = Context.Loop(lhs.size(), index)
            Visitor(Context(c.lib, loop, c.mc, node, c.om, c.pmap, c.rand, c.vars)).visit(rpt)?.let { rhs ->
                when (rhs) {
                    is ArrayNode -> rhs.forEach { element ->
                        result.add(element)
                    }

                    else -> result.add(rhs)
                }
            }
        }
        bindPositionalVariable(pvb, result)
        return bindContextualValue(cvb, result, lhs)
    }

    private fun stretch(value: BindContextNode, size: Int): BindContextNode {
        val result = c.createBindContextNode()
        val ratio = size / value.size()
        value.forEach { element ->
            repeat(ratio) {
                result.add(element)
            }
        }
        return result
    }

    private fun stretch(value: BindPositionNode, size: Int): BindPositionNode {
        val result = c.createBindPositionNode()
        val ratio = size / value.size()
        value.forEach { element ->
            repeat(ratio) {
                result.add(element)
            }
        }
        return result
    }

    private fun stretch(variables: MutableMap<String, JsonNode>, size: Int): MutableMap<String, JsonNode> {
        val result = mutableMapOf<String, JsonNode>()
        variables.forEach { (id, value) ->
            when (value) {
                is BindContextNode -> result[id] = stretch(value, size)
                is BindPositionNode -> result[id] = stretch(value, size)
                else -> result[id] = value
            }
        }
        return result
    }


    override fun visitArray(ctx: JSong3Parser.ArrayContext): ArrayNode {
        val result = c.createArrayNode()
        ctx.element().forEach { ctxElement ->
            Visitor(c).visit(ctxElement)?.let { element ->
                result.add(element)
            }
        }
        return result
    }

    override fun visitEvalAndOr(ctx: JSong3Parser.EvalAndOrContext): BooleanNode {
        val lhs = booleanOf(reduce(Visitor(c).visit(ctx.lhs)))
        val rhs = booleanOf(reduce(Visitor(c).visit(ctx.rhs)))
        return BooleanNode.valueOf(
            when (ctx.op.type) {
                JSong3Parser.AND -> lhs && rhs
                JSong3Parser.OR -> lhs || rhs
                else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
            }
        )
    }

    override fun visitEvalBlocks(ctx: JSong3Parser.EvalBlocksContext): JsonNode? {
        var exp = c.node
        ctx.exp()?.forEach { ctxExp ->
            exp = Visitor(Context(c.lib, null, c.mc, exp, c.om, c.pmap, c.rand, c.vars)).visit(ctxExp)
        }
        return reduce(exp)
    }

    override fun visitEvalCompare(ctx: JSong3Parser.EvalCompareContext): BooleanNode {
        val lhs = reduce(Visitor(c).visit(ctx.lhs))
        val rhs = reduce(Visitor(c).visit(ctx.rhs))
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
        val writer = c.om.writer()
        val lhs = stringOf(reduce(Visitor(c).visit(ctx.lhs)), writer)
        val rhs = stringOf(reduce(Visitor(c).visit(ctx.rhs)), writer)
        return TextNode(lhs.plus(rhs))
    }

    @Throws(UnsupportedOperationException::class)
    override fun visitEvalDivModMul(ctx: JSong3Parser.EvalDivModMulContext): DecimalNode {
        val lhs = decimalOf(reduce(Visitor(c).visit(ctx.lhs)))
        val rhs = decimalOf(reduce(Visitor(c).visit(ctx.rhs)))
        return DecimalNode(
            when (ctx.op.type) {
                JSong3Parser.DIV -> lhs.divide(rhs, c.mc)
                JSong3Parser.MOD -> lhs.remainder(rhs)
                JSong3Parser.MUL -> lhs.multiply(rhs)
                else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
            }
        )
    }

    @Throws(Exception::class)
    override fun visitEvalFunction(ctx: JSong3Parser.EvalFunctionContext): JsonNode? {
        val id = sanitise(ctx.`var`().ID().text)
        if (c.lib.has(id)) {
            val args = Array(ctx.exp().size) { i ->
                reduce(
                    Visitor(Context(c.lib, null, c.mc, c.node, c.om, c.pmap, c.rand, c.vars)).visit(ctx.exp()[i])
                )
            }
            return c.lib.call(id, *args)
        } else throw NoSuchMethodException("function $id not found")
    }

    @Throws(IllegalArgumentException::class)
    override fun visitEvalNegative(ctx: JSong3Parser.EvalNegativeContext): DecimalNode {
        return DecimalNode(decimalOf(reduce(Visitor(c).visit(ctx.exp()))).negate())
    }

    @Throws(UnsupportedOperationException::class)
    override fun visitEvalSumSub(ctx: JSong3Parser.EvalSumSubContext): DecimalNode {
        val lhs = decimalOf(reduce(Visitor(c).visit(ctx.lhs)))
        val rhs = decimalOf(reduce(Visitor(c).visit(ctx.rhs)))
        return DecimalNode(
            when (ctx.op.type) {
                JSong3Parser.SUB -> lhs.subtract(rhs)
                JSong3Parser.SUM -> lhs.add(rhs)
                else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
            }
        )
    }

    override fun visitEvalVariable(ctx: JSong3Parser.EvalVariableContext): JsonNode? {
        val id = sanitise(ctx.`var`().ID().text)
        return when (val result = c.vars[id]) {
            is BindContextNode -> when (c.loop != null) {
                true -> result.get(c.loop)
                else -> result
            }

            is BindPositionNode -> when (c.loop != null) {
                true -> result.get(c.loop)
                else -> result
            }

            else -> result
        }
    }

    override fun visitFalse(ctx: JSong3Parser.FalseContext): BooleanNode {
        return BooleanNode.FALSE
    }

    override fun visitFilter(ctx: JSong3Parser.FilterContext): ArrayNode {
        val lhs = expand(Visitor(c).visit(ctx.lhs))
        val loop = Context.Loop(lhs.size() * (c.loop?.size ?: 1))
        val predicates = BooleanArray(loop.size)
        c.vars = stretch(c.vars, loop.size)
        lhs.forEachIndexed { index, node ->
            loop.index = if (c.loop != null) {
                c.loop.size * c.loop.index + index
            } else index
            Visitor(Context(c.lib, loop, c.mc, node, c.om, c.pmap, c.rand, c.vars)).visit(ctx.rhs)
                ?.let { rhs ->
                    when (rhs) {
                        is ArrayNode -> {
                            val indexes = RangeNode.indexes(rhs)
                            when (indexes.isNotEmpty()) {
                                true -> predicates[index] = indexes.contains(index)
                                else -> predicates[index] = booleanOf(rhs)
                            }
                        }

                        is NumericNode -> {
                            val value = rhs.asInt()
                            val offset = if (value < 0) loop.size + value else value
                            predicates[index] = index == offset
                        }

                        else -> predicates[index] = booleanOf(rhs)
                    }
                }
            loop.index++
        }
        c.vars = filter(c.vars, predicates)
        return filter(lhs, predicates)
    }

    override fun visitGotoContext(ctx: JSong3Parser.GotoContextContext): JsonNode? {
        return c.node
    }

    override fun visitGotoId(ctx: JSong3Parser.GotoIdContext): JsonNode? {
        val fieldName = sanitise(ctx.ID().text)
        return if (c.node is ObjectNode && c.node.has(fieldName)) {
            val result = c.node[fieldName]
            if (result is ArrayNode) result.forEach { element ->
                c.pmap(c.node, element)
            }
            c.pmap(c.node, result)
        } else null
    }

    override fun visitGotoParent(ctx: JSong3Parser.GotoParentContext): JsonNode? {
        var result: JsonNode? = c.node
        var steps = ctx.MOD().size
        while (steps > 0 && result != null) {
            result = c.pmap[result]
            steps--
        }
        return result
    }

    override fun visitGotoWildcard(ctx: JSong3Parser.GotoWildcardContext?): JsonNode? {
        return if (c.node is ObjectNode) {
            val result = c.createArrayNode()
            c.node.fields().forEach { field ->
                if (field.value != null) {
                    result.add(field.value)
                }
            }
            c.pmap(c.node, result)
        } else null
    }

    override fun visitGotoWildDescendants(ctx: JSong3Parser.GotoWildDescendantsContext): JsonNode? {
        return if (c.node is ObjectNode) {
            c.pmap(c.node, descendants(c.node))
        } else null
    }

    override fun visitJsong(ctx: JSong3Parser.JsongContext): JsonNode? {
        var exp = c.node
        ctx.exp()?.forEach { ctxExp ->
            exp = Visitor(Context(c.lib, null, c.mc, exp, c.om, c.pmap, c.rand, c.vars)).visit(ctxExp)
        }
        return reduce(exp)
    }


    override fun visitMap(ctx: JSong3Parser.MapContext): ArrayNode {
        return map(ctx.lhs, ctx.rhs)

    }

    override fun visitMapCvb(ctx: JSong3Parser.MapCvbContext): ArrayNode {
        return map(ctx.lhs, ctx.rhs, null, ctx.cvb())
    }

    override fun visitMapPvb(ctx: JSong3Parser.MapPvbContext): ArrayNode {
        return map(ctx.lhs, ctx.rhs, ctx.pvb())
    }

    override fun visitMapPvbCvb(ctx: JSong3Parser.MapPvbCvbContext): ArrayNode {
        return map(ctx.lhs, ctx.rhs, ctx.pvb(), ctx.cvb())
    }

    override fun visitNull(ctx: JSong3Parser.NullContext?): NullNode {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSong3Parser.NumberContext): DecimalNode {
        return DecimalNode(ctx.NUMBER().text.toBigDecimal())
    }

    override fun visitObject(ctx: JSong3Parser.ObjectContext): ObjectNode {
        val result = c.createObjectNode()
        ctx.field().forEachIndexed { index, fieldCtx ->
            val key = reduce(Visitor(c).visit(fieldCtx.key))?.asText() ?: index.toString()
            val value = reduce(Visitor(c).visit(fieldCtx.`val`))
            result.set<JsonNode>(key, value)
        }
        return result
    }

    override fun visitRange(ctx: JSong3Parser.RangeContext): RangeNode {
        val lhs = decimalOf(reduce(Visitor(c).visit(ctx.lhs)))
        val rhs = decimalOf(reduce(Visitor(c).visit(ctx.rhs)))
        return RangeNode.between(lhs, rhs, c.om)
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