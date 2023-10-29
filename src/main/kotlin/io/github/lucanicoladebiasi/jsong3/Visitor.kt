package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong3BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Parser
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


    override fun visit(tree: ParseTree?): JsonNode? {
        return reduce(super.visit(tree))
    }

    override fun visitArray(ctx: JSong3Parser.ArrayContext): ArrayNode {
        val result = mapper.createArrayNode()
        ctx.element().forEach { element ->
            Visitor(context, loop, mapper, mathContext, variables).visit(element)?.let { result.add(it) }
        }
        return result
    }

    override fun visitBlock(ctx: JSong3Parser.BlockContext): JsonNode? {
        var result: JsonNode? = null
        ctx.exp().forEach { exp ->
            result = Visitor(context, loop, mapper, mathContext, variables).visit(exp)
        }
        return result
    }

    override fun visitCompare(ctx: JSong3Parser.CompareContext): BooleanNode {
        val lhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs)
        val rhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs)
        return if (lhs != null && rhs != null) {
            val comparison = compare(lhs, rhs)
            BooleanNode.valueOf(
                when (ctx.op.type) {
                    JSong3Parser.LT -> comparison < 0
                    JSong3Parser.LE -> comparison <= 0
                    JSong3Parser.GE -> comparison >= 0
                    JSong3Parser.GT -> comparison > 0
                    JSong3Parser.NE -> comparison != 0
                    JSong3Parser.EQ -> comparison == 0
                    else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
                }
            )
        } else BooleanNode.FALSE
    }

    override fun visitContext(ctx: JSong3Parser.ContextContext): JsonNode? {
        return context
    }

    override fun visitFilter(ctx: JSong3Parser.FilterContext): ArrayNode {
        println("FILTER LHS = ${ctx.lhs.text} PRD = ${ctx.prd.text}")
        val lhs = expand(mapper, Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs))
        val filters = Array(lhs.size()) { false }
        lhs.forEachIndexed { index, context ->
            when (val prd = Visitor(context, index, mapper, mathContext, variables).visit(ctx.prd)) {
                is NumericNode -> filters[index] = predicate(prd, index, lhs.size())
                is RangeNode -> filters[index] = predicate(prd, index, lhs.size())
                else -> filters[index] = predicate(prd)
            }
        }
        return filter(mapper, filters, lhs)
    }

//    override fun visitFilter(ctx: JSong3Parser.FilterContext): ArrayNode {
//        println("FILTER LHS = ${ctx.lhs.text} RHS = ${ctx.rhs.text}")
//        val lhs = expand(mapper, Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs))
//        val stretch = stretch(mapper, lhs.size(), variables)
//        val predicates = Array(lhs.size()) { false }
//        lhs.forEachIndexed { index, context ->
//            when(val rhs = Visitor(context, index, mapper, mathContext, stretch).visit(ctx.rhs)) {
//                is NumericNode -> predicates[index] = predicate(rhs, index, lhs.size())
//                is RangeNode -> predicates[index] = predicate(rhs, index, lhs.size())
//                else -> predicates[index] = predicate(rhs)
//            }
//        }
//        stretch.forEach { (name, variable) ->
//            when(variable) {
//                is BindContextNode -> variables[name] = BindContextNode(mapper).addAll(filter(mapper, predicates, variable))
//                is BindPositionNode -> variables[name] = BindPositionNode(mapper).addAll(filter(mapper, predicates, variable))
//                else -> variables[name] = variable
//            }
//        }
//        return filter(mapper, predicates, lhs)
//    }

    override fun visitFalse(ctx: JSong3Parser.FalseContext): BooleanNode {
        return BooleanNode.FALSE
    }

    override fun visitId(ctx: JSong3Parser.IdContext): JsonNode? {
        val fieldName = sanitise(ctx.ID().text)
        return if (context is ObjectNode && context.has(fieldName)) {
            context[fieldName]
        } else
            null
    }

    override fun visitInclude(ctx: JSong3Parser.IncludeContext): BooleanNode {
        val lhs = reduce(Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs))
        val rhs = expand(mapper, Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs))
        return BooleanNode.valueOf(rhs.contains(lhs))
    }

    override fun visitJsong(ctx: JSong3Parser.JsongContext): JsonNode? {
        return ctx.exp()?.let { exp ->
            visit(exp)
        }
    }

    override fun visitLogic(ctx: JSong3Parser.LogicContext): BooleanNode {
        val lhs = predicate(reduce(Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs)))
        val rhs = predicate(reduce(Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs)))
        val boolean = when (ctx.op.type) {
            JSong3Parser.AND -> lhs && rhs
            JSong3Parser.OR -> lhs || rhs
            else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
        }
        return BooleanNode.valueOf(boolean)
    }

    override fun visitMap(ctx: JSong3Parser.MapContext): ArrayNode {
        println("MAP LHS = ${ctx.lhs.text} RHS = ${ctx.rhs.text}")
        val result = mapper.createArrayNode()
        val lhs = expand(mapper, Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs))
        lhs.forEachIndexed { index, context ->
            when (context) {
                is RangeNode -> context.indexes.forEach { i ->
                    val rhs = Visitor(IntNode(i), index, mapper, mathContext, variables).visit(ctx.rhs)
                    if (rhs != null) when (rhs) {
                        is ArrayNode -> result.addAll(rhs)
                        else -> result.add(rhs)
                    }
                }

                else -> {
                    val rhs = Visitor(context, index, mapper, mathContext, variables).visit(ctx.rhs)
                    if (rhs != null) when (rhs) {
                        is ArrayNode -> result.addAll(rhs)
                        else -> result.add(rhs)
                    }
                }
            }
        }
        if (ctx.op.isNotEmpty()) {
            ctx.op.forEachIndexed { opIndex, op ->
                val id = sanitise(ctx.VAR_ID(opIndex).text)
                when(op.type) {
                    JSong3Parser.AT -> variables[id] = BindContextNode(mapper).addAll(result)
                    JSong3Parser.HASH -> variables[id] = BindPositionNode(mapper).addAll(result)
                }
            }
            if (ctx.op.last().type == JSong3Parser.AT) {
                return stretch(mapper, result.size(), lhs)
            }
        }
        return result
    }

//    override fun visitMapAndBind(ctx: JSong3Parser.MapAndBindContext): ArrayNode {
//        println("BIND LHS = ${ctx.lhs.text} RHS = ${ctx.rhs.text}")
//        val result = mapper.createArrayNode()
//        val lhs = expand(mapper, Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs))
//        lhs.forEachIndexed { index, context ->
//            when (context) {
//                is RangeNode -> context.indexes.forEach { i ->
//                    val rhs = Visitor(IntNode(i), index, mapper, mathContext, variables).visit(ctx.rhs)
//                    if (rhs != null) when (rhs) {
//                        is ArrayNode -> result.addAll(rhs)
//                        else -> result.add(rhs)
//                    }
//                }
//
//                else -> {
//                    val rhs = Visitor(context, index, mapper, mathContext, variables).visit(ctx.rhs)
//                    if (rhs != null) when (rhs) {
//                        is ArrayNode -> result.addAll(rhs)
//                        else -> result.add(rhs)
//                    }
//                }
//            }
//        }
//        if (ctx.op.isNotEmpty()) {
//            ctx.op.forEachIndexed { i, op ->
//                val id = sanitise(ctx.VAR_ID()[i].text)
//                when (op.type) {
//                    JSong3Parser.AT -> variables[id] = BindContextNode(mapper).addAll(result)
//                    JSong3Parser.HASH -> variables[id] = BindPositionNode(mapper).addAll(result)
//                }
//            }
//            if (ctx.op.last().type == JSong3Parser.AT) {
//                val carry = mapper.createArrayNode()
//                repeat(result.size() / lhs.size()) {
//                    carry.addAll(lhs)
//                }
//                return carry
//            }
//        }
//        return result
//    }

    override fun visitMathMULorDIVorMOD(ctx: JSong3Parser.MathMULorDIVorMODContext): DecimalNode {
        val lhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs)
        if (lhs != null && lhs.isNumber) {
            val rhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs)
            if (rhs != null && rhs.isNumber) {
                val value = when (ctx.op.type) {
                    JSong3Parser.MUL -> lhs.decimalValue().multiply(rhs.decimalValue())
                    JSong3Parser.DIV -> lhs.decimalValue().divide(rhs.decimalValue(), mathContext)
                    JSong3Parser.MOD -> lhs.decimalValue().remainder(rhs.decimalValue())
                    else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
                }
                return DecimalNode(value)
            } else throw IllegalArgumentException("RHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
        } else throw IllegalArgumentException("LHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
    }

    override fun visitMathSUMorSUB(ctx: JSong3Parser.MathSUMorSUBContext): DecimalNode {
        val lhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs)
        if (lhs != null && lhs.isNumber) {
            val rhs = Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs)
            if (rhs != null && rhs.isNumber) {
                val value = when (ctx.op.type) {
                    JSong3Parser.SUM -> lhs.decimalValue().add(rhs.decimalValue())
                    JSong3Parser.SUB -> lhs.decimalValue().subtract(rhs.decimalValue())
                    else -> throw UnsupportedOperationException("unknown operator in ${ctx.text} expression")
                }
                return DecimalNode(value)
            } else throw IllegalArgumentException("RHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
        } else throw IllegalArgumentException("LHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
    }

    override fun visitNull(ctx: JSong3Parser.NullContext?): NullNode {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSong3Parser.NumberContext): DecimalNode {
        val value = ctx.NUMBER().text.toBigDecimal()
        return DecimalNode(
            when (ctx.SUB() == null) {
                true -> value
                else -> value.negate()
            }
        )
    }

    override fun visitObject(ctx: JSong3Parser.ObjectContext): ObjectNode {
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

    override fun visitRange(ctx: JSong3Parser.RangeContext): RangeNode {
        return RangeNode.between(
            Visitor(context, loop, mapper, mathContext, variables).visit(ctx.min)?.decimalValue()
                ?: BigDecimal.ZERO,
            Visitor(context, loop, mapper, mathContext, variables).visit(ctx.max)?.decimalValue()
                ?: BigDecimal.ZERO,
            mapper
        )
    }

    override fun visitRegex(ctx: JSong3Parser.RegexContext): RegexNode {
        return RegexNode.of(ctx.text)
    }

    override fun visitRegexCI(ctx: JSong3Parser.RegexCIContext): RegexNode {
        return RegexNode.ci(ctx.text)
    }

    override fun visitRegexML(ctx: JSong3Parser.RegexMLContext): RegexNode {
        return RegexNode.ml(ctx.text)
    }

    override fun visitText(ctx: JSong3Parser.TextContext): TextNode {
        return TextNode(sanitise(ctx.STRING().text))
    }

    override fun visitTrue(ctx: JSong3Parser.TrueContext?): BooleanNode {
        return BooleanNode.TRUE
    }

    override fun visitVar(ctx: JSong3Parser.VarContext): JsonNode? {
        val id = sanitise(ctx.VAR_ID().text)
        val variable = if (loop != null) {
            expand(mapper, variables[id])[loop]
        } else variables[id]
        return when (variable) {
            is BindPositionNode.PositionNode -> variable.position
            else -> variable
        }
    }

}//~ Visitor