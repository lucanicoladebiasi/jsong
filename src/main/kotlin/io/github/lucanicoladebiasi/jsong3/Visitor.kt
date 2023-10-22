package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.apache.commons.text.StringEscapeUtils
import java.math.BigDecimal
import java.math.MathContext

class Visitor(
    private val context: JsonNode?,
    private val loop: Int?,
    private val mapper: ObjectMapper,
    private val mathContext: MathContext,
    private val variables: MutableMap<String, JsonNode>
) : JSong2BaseVisitor<JsonNode?>() {

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

        private fun index(node: JsonNode?, max: Int, mapper: ObjectMapper): Set<Int> {
            val result = mutableSetOf<Int>()
            when (node) {
                is ArrayNode -> node.forEach { element ->
                    result.addAll(index(element, max, mapper))
                }

                is NumericNode -> {
                    val value = node.asInt()
                    result.add(if (value < 0) max + value else value)
                }

                is RangeNode -> node.indexes.forEach { value ->
                    result.add(if (value < 0) max + value else value)
                }
            }
            return result.sorted().toSet()
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


        private fun stretch(mapper: ObjectMapper, size: Int, variables: Map<String, JsonNode>): MutableMap<String, JsonNode> {
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
                    else -> map[name] = variable
                }
            }
            return map
        }

    } //~ companion


    override fun visit(tree: ParseTree?): JsonNode? {
        return reduce(super.visit(tree))
    }

    override fun visitArray(ctx: JSong2Parser.ArrayContext): ArrayNode {
        val result = mapper.createArrayNode()
        ctx.element().forEach { element ->
            Visitor(context, loop, mapper, mathContext, variables).visit(element)?.let { result.add(it) }
        }
        return result
    }

    override fun visitBlock(ctx: JSong2Parser.BlockContext): JsonNode? {
        var result: JsonNode? = null
        ctx.exp().forEach { exp ->
            result = Visitor(context, loop, mapper, mathContext, variables).visit(exp)
        }
        return result
    }

    override fun visitCompare(ctx: JSong2Parser.CompareContext): BooleanNode {
        val lhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs)
        val rhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs)
        return if (lhs != null && rhs != null) {
            val comparison = compare(lhs, rhs)
            BooleanNode.valueOf(
                when (ctx.op.type) {
                    JSong2Parser.LT -> comparison < 0
                    JSong2Parser.LE -> comparison <= 0
                    JSong2Parser.GE -> comparison >= 0
                    JSong2Parser.GT -> comparison > 0
                    JSong2Parser.NE -> comparison != 0
                    JSong2Parser.EQ -> comparison == 0
                    else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
                }
            )
        } else BooleanNode.FALSE
    }

    override fun visitContext(ctx: JSong2Parser.ContextContext): JsonNode? {
        return context
    }

//            val indexes = index(rhs, lhs.size(), mapper) // todo check if filters and indexes are the same thing
//            if (indexes.isNotEmpty()) {
//                if (indexes.contains(index)) {
//                    result.add(context)
//                }
//            } else if (predicate(rhs)) {
//                filters.add(index)
//                result.add(context)
//            }

    override fun visitFilter(ctx: JSong2Parser.FilterContext): ArrayNode {
        val lhs = expand(mapper, Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs))
        val stretch = stretch(mapper, lhs.size(), variables)
        val predicates = Array(lhs.size()) { false }
        lhs.forEachIndexed { index, context ->
            val rhs = Visitor(context, index, mapper, mathContext, stretch).visit(ctx.rhs)
            predicates[index] = predicate(rhs)
        }
        stretch.forEach { (name, variable) ->
            when(variable) {
                is BindContextNode -> variables[name] = BindContextNode(mapper).addAll(filter(mapper, predicates, variable))
                else -> variables[name] = variable
            }
        }
        return filter(mapper, predicates, lhs)
    }

    override fun visitFalse(ctx: JSong2Parser.FalseContext): BooleanNode {
        return BooleanNode.FALSE
    }

    override fun visitId(ctx: JSong2Parser.IdContext): JsonNode? {
        val fieldName = sanitise(ctx.ID().text)
        return if (context is ObjectNode && context.has(fieldName)) {
            context[fieldName]
        } else
            null
    }

    override fun visitInclude(ctx: JSong2Parser.IncludeContext): BooleanNode {
        val lhs = reduce(Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs))
        val rhs = expand(mapper, Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs))
        return BooleanNode.valueOf(rhs.contains(lhs))
    }

    override fun visitJsong(ctx: JSong2Parser.JsongContext): JsonNode? {
        return ctx.exp()?.let { exp ->
            visit(exp)
        }
    }

    override fun visitMap(ctx: JSong2Parser.MapContext): ArrayNode {
        val result = mapper.createArrayNode()
        val lhs = expand(mapper, Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs))
        lhs.forEachIndexed { index, context ->
            val stretch = stretch(mapper, lhs.size(), variables)
            when (context) {
                is RangeNode -> context.indexes.forEach { i ->
                    val rhs = Visitor(IntNode(i), index, mapper, mathContext, stretch).visit(ctx.rhs)
                    if (rhs != null) when (rhs) {
                        is ArrayNode -> result.addAll(rhs)
                        else -> result.add(rhs)
                    }
                }

                else -> {
                    val rhs = Visitor(context, index, mapper, mathContext, stretch).visit(ctx.rhs)
                    if (rhs != null) when (rhs) {
                        is ArrayNode -> result.addAll(rhs)
                        else -> result.add(rhs)
                    }
                }
            }
        }
        return result
    }

    override fun visitMapAndBind(ctx: JSong2Parser.MapAndBindContext): ArrayNode {
        val result = mapper.createArrayNode()
        val lhs = expand(mapper, Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs))
        lhs.forEachIndexed { index, context ->
            when (context) {
                is RangeNode -> context.indexes.forEach { i ->
                    val rhs = Visitor(IntNode(i), index, mapper, mathContext, stretch(mapper, lhs.size(), variables)).visit(ctx.rhs)
                    if (rhs != null) when (rhs) {
                        is ArrayNode -> result.addAll(rhs)
                        else -> result.add(rhs)
                    }
                }

                else -> {
                    val rhs = Visitor(context, index, mapper, mathContext, stretch(mapper, lhs.size(), variables)).visit(ctx.rhs)
                    if (rhs != null) when (rhs) {
                        is ArrayNode -> result.addAll(rhs)
                        else -> result.add(rhs)
                    }
                }
            }
        }
        if (ctx.op.isNotEmpty()) {
            ctx.op.forEachIndexed { i, op ->
                val id = sanitise(ctx.VAR_ID()[i].text)
                when (op.type) {
                    JSong2Parser.AT -> variables[id] = BindContextNode(mapper).addAll(result)
                    JSong2Parser.HASH -> TODO()
                }
            }
            if (ctx.op.last().type == JSong2Parser.AT) {
                val carry = mapper.createArrayNode()
                repeat(result.size() / lhs.size()) {
                    carry.addAll(lhs)
                }
                return carry
            }
        }
        return result
    }

    override fun visitMathMULorDIVorMOD(ctx: JSong2Parser.MathMULorDIVorMODContext): DecimalNode {
        val lhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs)
        if (lhs != null && lhs.isNumber) {
            val rhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs)
            if (rhs != null && rhs.isNumber) {
                val value = when (ctx.op.type) {
                    JSong2Parser.MUL -> lhs.decimalValue().multiply(rhs.decimalValue())
                    JSong2Parser.DIV -> lhs.decimalValue().divide(rhs.decimalValue(), mathContext)
                    JSong2Parser.MOD -> lhs.decimalValue().remainder(rhs.decimalValue())
                    else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
                }
                return DecimalNode(value)
            } else throw IllegalArgumentException("RHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
        } else throw IllegalArgumentException("LHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
    }

    override fun visitMathSUMorSUB(ctx: JSong2Parser.MathSUMorSUBContext): DecimalNode {
        val lhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs)
        if (lhs != null && lhs.isNumber) {
            val rhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs)
            if (rhs != null && rhs.isNumber) {
                val value = when (ctx.op.type) {
                    JSong2Parser.SUM -> lhs.decimalValue().add(rhs.decimalValue())
                    JSong2Parser.SUB -> lhs.decimalValue().subtract(rhs.decimalValue())
                    else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
                }
                return DecimalNode(value)
            } else throw IllegalArgumentException("RHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
        } else throw IllegalArgumentException("LHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
    }

    override fun visitNull(ctx: JSong2Parser.NullContext?): NullNode {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSong2Parser.NumberContext): DecimalNode {
        val value = ctx.NUMBER().text.toBigDecimal()
        return DecimalNode(
            when (ctx.SUB() == null) {
                true -> value
                else -> value.negate()
            }
        )
    }

    override fun visitObject(ctx: JSong2Parser.ObjectContext): ObjectNode {
        val result = mapper.createObjectNode()
        ctx.field().forEachIndexed { index, field ->
            val key = Visitor(context, loop, mapper, mathContext, variables).visit(field.key)?.asText()
                ?: index.toString()
            val value = Visitor(context, loop, mapper, mathContext, variables).visit(field.`val`)
                ?: NullNode.instance
            result.set<JsonNode>(key, value)
        }
        return result
    }

    override fun visitRange(ctx: JSong2Parser.RangeContext): RangeNode {
        return RangeNode.between(
            Visitor(context, loop, mapper, mathContext, variables).visit(ctx.min)?.decimalValue()
                ?: BigDecimal.ZERO,
            Visitor(context, loop, mapper, mathContext, variables).visit(ctx.max)?.decimalValue()
                ?: BigDecimal.ZERO,
            mapper
        )
    }

    override fun visitText(ctx: JSong2Parser.TextContext): TextNode {
        return TextNode(sanitise(ctx.STRING().text))
    }

    override fun visitTrue(ctx: JSong2Parser.TrueContext?): BooleanNode {
        return BooleanNode.TRUE
    }

    override fun visitVar(ctx: JSong2Parser.VarContext): JsonNode? {
        val id = sanitise(ctx.VAR_ID().text)
        val result = variables[id]
        if (loop != null) {
            return expand(mapper, result)[loop]
        }
        return result
    }

}//~ Visitor