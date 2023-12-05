package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import io.github.lucanicoladebiasi.jsong.antlr.JSong3BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Parser
import io.github.lucanicoladebiasi.jsong2.Processor
import io.github.lucanicoladebiasi.jsong3.functions.NumericFunctions.Companion.decimalOf
import io.github.lucanicoladebiasi.jsong3.functions.StringFunctions.Companion.stringOf
import org.apache.commons.text.StringEscapeUtils

class Visitor(
    private val context: Context
) : JSong3BaseVisitor<JsonNode?>() {

    companion object {

        private fun compare(lhs: JsonNode, rhs: JsonNode): Int {
            return when {
                lhs.isNumber && rhs.isNumber -> lhs.decimalValue().compareTo(rhs.decimalValue())
                else -> lhs.textValue().compareTo(rhs.textValue())
            }
        }

        private fun expand(mapper: ObjectMapper, node: JsonNode?): ArrayNode {
            val result = mapper.createArrayNode()
            if (node != null) when (node) {
                is ArrayNode -> result.addAll(node)
                else -> result.add(node)
            }
            return result
        }

        private fun filter(mapper: ObjectMapper, predicates: Array<Boolean>, arrayNode: ArrayNode): ArrayNode {
            val result = mapper.createArrayNode()
            if (predicates.size == arrayNode.size()) {
                predicates.forEachIndexed { index, predicate ->
                    if (predicate) {
                        result.add(arrayNode[index])
                    }
                }
            } else TODO("exception not same size!")
            return result
        }

        private fun predicate(node: JsonNode?): Boolean {
            when (node) {
                null -> return false
                is ArrayNode -> {
                    node.forEach { element ->
                        if (predicate(element))
                            return true
                    }
                    return false
                }

                is BooleanNode -> return node.booleanValue()
                is NumericNode -> return node.decimalValue() != DecimalNode.ZERO
                is ObjectNode -> return node.size() > 0
                is TextNode -> return node.textValue().isNotEmpty()
                else -> return false
            }
        }

        private fun predicate(node: NumericNode, index: Int, size: Int): Boolean {
            val v = node.asInt()
            return index == if (v < 0) size + v else v
        }

        private fun predicate(node: RangeNode, index: Int, size: Int): Boolean {
            return node.indexes.map { v -> if (v < 0) size + v else v }.contains(index)
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


        private fun stretch(mapper: ObjectMapper, size: Int, node: JsonNode): ArrayNode {
            val result = mapper.createArrayNode()
            val array = expand(mapper, node)
            repeat(size / array.size()) {
                result.addAll(array)
            }
            return result
        }


        private fun stretch(
            mapper: ObjectMapper,
            size: Int,
            variables: Map<String, JsonNode>
        ): MutableMap<String, JsonNode> {
            val map = mutableMapOf<String, JsonNode>()
            variables.forEach { (name, variable) ->
                when (variable) {
                    is BindContextNode -> {
                        // todo check what if size < variable.size or not multiples
                        if (size > variable.size()) {
                            val carry = BindContextNode(mapper)
                            val ratio = size / variable.size()
                            variable.forEach { element ->
                                repeat(ratio) {
                                    carry.add(element)
                                }
                            }
                            map[name] = carry
                        } else map[name] = variable
                    }

                    is BindPositionNode -> {
                        // todo check what if size < variable.size or not multiples
                        if (size > variable.size()) {
                            val carry = BindPositionNode(mapper)
                            val ratio = size / variable.size()
                            variable.forEach { element ->
                                repeat(ratio) {
                                    carry.add(element)
                                }
                            }
                        } else map[name] = variable
                    }

                    else -> map[name] = variable
                }
            }
            return map
        }

    } //~ companion

    override fun visitArray(ctx: JSong3Parser.ArrayContext): JsonNode {
        val result = context.mapper.createArrayNode()
        ctx.element().forEach { elementCtx ->
            Visitor(context).visit(elementCtx)?.let { element ->
                result.add(element)
            }
        }
        return result
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
        return expand(context.mapper, context.node)
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
        var result = context.node
        ctx.exp()?.forEach { expCtx ->
            result = Visitor(context).visit(expCtx)
        }
        return reduce(result)
    }

    override fun visitNull(ctx: JSong3Parser.NullContext?): NullNode {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSong3Parser.NumberContext): DecimalNode {
        return DecimalNode(ctx.NUMBER().text.toBigDecimal())
    }

    override fun visitObject(ctx: JSong3Parser.ObjectContext): ObjectNode {
        val result = context.mapper.createObjectNode()
        ctx.field().forEachIndexed { index, fieldCtx ->
            val key = reduce(Visitor(context).visit(fieldCtx.key))?.asText()
                ?: index.toString()
            val value = Visitor(context).visit(fieldCtx.`val`)
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


}//~ Visitor