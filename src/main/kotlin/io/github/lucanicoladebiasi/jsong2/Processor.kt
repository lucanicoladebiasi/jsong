package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils
import java.math.BigDecimal
import java.math.MathContext

class Processor(
    root: JsonNode?,
    mapper: ObjectMapper,
    private val mc: MathContext
) : JSong2BaseVisitor<JsonNode?>() {

    companion object {

        private fun back(node: JsonNode, step: Int, parents: Map<JsonNode, JsonNode>): JsonNode? {
            return when (step) {
                0 -> {
                    node
                }

                else -> {
                    parents[node]?.let { back(it, step - 1, parents) }
                }
            }
        }

        private fun compare(lhs: JsonNode?, rhs: JsonNode?): Int {
            return when {
                lhs == null && rhs == null -> 0
                lhs == null -> -1
                rhs == null -> 1
                lhs.isNumber && rhs.isNumber -> lhs.decimalValue().compareTo(rhs.decimalValue())
                else -> lhs.textValue().compareTo(rhs.textValue())
            }
        }

        private fun expand(nf: JsonNodeFactory, node: JsonNode?): ArrayNode {
            val array = ArrayNode(nf)
            if (node != null) when (node) {
                is ArrayNode -> array.addAll(node)
                else -> array.add(node)
            }
            return array
        }

        private fun predicate(node: JsonNode?): Boolean {
            when(node) {
                null -> return false
                is ArrayNode -> {
                    node.forEach { argument ->
                        if (predicate(argument)) {
                            return true
                        }
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

        internal fun reduce(node: JsonNode?): JsonNode? {
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

    private var context: JsonNode? = root

    private val nf = mapper.nodeFactory

    private val parents = mutableMapOf<JsonNode, JsonNode>()

    override fun visitArray(ctx: JSong2Parser.ArrayContext): ArrayNode {
        val array = ArrayNode(nf)
        ctx.element().forEach { element ->
            val context = this.context
            when {
                element.exp() != null -> array.add(visit(element.exp()))
                element.range() != null -> array.add(visit(element.range()))
            }
            this.context = context
        }
        return array
    }

    override fun visitBlock(ctx: JSong2Parser.BlockContext): JsonNode? {
        var rs: JsonNode? = null
        ctx.exp().forEach { exp ->
            rs = reduce(visit(exp))
        }
        return rs
    }

    override fun visitCompare(ctx: JSong2Parser.CompareContext): BooleanNode {
        val context = this.context
        val lhs = reduce(visit(ctx.lhs))
        this.context = context
        val rhs = reduce(visit(ctx.rhs))
        this.context = context
        val value = compare(lhs, rhs)
        return BooleanNode.valueOf(
            when (ctx.op.type) {
                JSong2Parser.LT -> value < 0
                JSong2Parser.LE -> value <= 0
                JSong2Parser.GE -> value >= 0
                JSong2Parser.GT -> value > 0
                JSong2Parser.NE -> value != 0
                JSong2Parser.EQ -> value == 0
                else -> throw UnsupportedOperationException("Unknown operator in ${ctx.text} expression!")
            }
        )
    }

    override fun visitContext(ctx: JSong2Parser.ContextContext): JsonNode? {
        return this.context
    }

    override fun visitFalse(ctx: JSong2Parser.FalseContext): BooleanNode {
        return BooleanNode.FALSE
    }

//    override fun visitFilter(ctx: JSong2Parser.FilterContext): ArrayNode {
//        val array = ArrayNode(nf)
//        val lhs = expand(nf, visit(ctx.lhs))
//        val rhs = visit(ctx.rhs)
//        val indexes = RangeNode.indexes(rhs)
//        if (indexes.isNotEmpty()) {
//            indexes.forEach { index ->
//                select(lhs, index)?.let { array.add(it) }
//            }
//        } else {
//            println(rhs)
//        }
//        return array
//    }

    override fun visitFilter(ctx: JSong2Parser.FilterContext): ArrayNode {
        val rs = ArrayNode(nf)
        val lhs = expand(nf, visit(ctx.lhs))
        lhs.forEachIndexed { index, context ->
            this.context = context
            val rhs = expand(nf, visit(ctx.rhs))
            rhs.forEach { argument ->
                when(argument) {
                    is NumericNode -> {
                        val value = argument.asInt()
                        val offset = if (value < 0) lhs.size() + value else value
                        if (index == offset) {
                            rs.add(context)
                        }
                    }
                    is RangeNode -> argument.indexes.forEach { value ->
                        val offset = if (value < 0) lhs.size() + value else value
                        if (index == offset) {
                            rs.add(context)
                        }
                    }
                    else -> if (predicate(argument)) {
                        rs.add(context)
                    }
                }
            }
        }
        return rs
    }

    override fun visitId(ctx: JSong2Parser.IdContext): JsonNode? {
        val fieldName = sanitise(ctx.ID().text)
        val context = this.context
        return if (context is ObjectNode && context.has(fieldName)) {
            return context[fieldName]
        } else null
    }

    override fun visitInclude(ctx: JSong2Parser.IncludeContext): BooleanNode {
        val context = this.context
        val lhs = reduce(visit(ctx.lhs))
        this.context = context
        val rhs = expand(nf, visit(ctx.rhs))
        this.context = context
        return BooleanNode.valueOf(rhs.contains(lhs))
    }

    override fun visitJsong(ctx: JSong2Parser.JsongContext): JsonNode? {
        var rs: JsonNode? = null
        ctx.exp()?.let { exp ->
            rs = visit(exp)
        }
        return rs
    }

    override fun visitMap(ctx: JSong2Parser.MapContext): ArrayNode {
        val rs = ArrayNode(nf)
        var lhs = expand(nf, visit(ctx.lhs))
        val indexes = RangeNode.indexes(lhs)
        if (indexes.isNotEmpty()) {
            lhs = ArrayNode(nf).addAll(indexes.map { IntNode(it) })
        }
        lhs.forEach { context ->
            this.context = context
            when (val rhs = visit(ctx.rhs)) {
                is ArrayNode -> rhs.forEach {
                    rs.add(it)
                    parents[it] = context
                }

                else -> rhs?.let {
                    rs.add(rhs)
                    parents[rhs] = context
                }
            }
        }
        return rs
    }

    override fun visitMathSUMorSUB(ctx: JSong2Parser.MathSUMorSUBContext): DecimalNode {
        val context = this.context
        val lhs = reduce(visit(ctx.lhs))
        if (lhs != null && lhs.isNumber) {
            this.context = context
            val rhs = reduce(visit(ctx.rhs))
            if (rhs != null && rhs.isNumber) {
                this.context = context
                val value = when (ctx.op.type) {
                    JSong2Parser.SUM -> lhs.decimalValue().add(rhs.decimalValue())
                    JSong2Parser.SUB -> lhs.decimalValue().subtract(rhs.decimalValue())
                    else -> throw UnsupportedOperationException("Unknown operator in ${ctx.text} expression!")
                }
                return DecimalNode(value)
            } else throw IllegalArgumentException("RHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
        } else throw IllegalArgumentException("RHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
    }

    override fun visitMathMULorDIVorMOD(ctx: JSong2Parser.MathMULorDIVorMODContext): JsonNode {
        val context = this.context
        val lhs = reduce(visit(ctx.lhs))
        if (lhs != null && lhs.isNumber) {
            this.context = context
            val rhs = reduce(visit(ctx.rhs))
            if (rhs != null && rhs.isNumber) {
                this.context = context
                val value = when (ctx.op.type) {
                    JSong2Parser.MUL -> lhs.decimalValue().multiply(rhs.decimalValue())
                    JSong2Parser.DIV -> lhs.decimalValue().divide(rhs.decimalValue(), mc)
                    JSong2Parser.MOD -> lhs.decimalValue().remainder(rhs.decimalValue())
                    else -> throw UnsupportedOperationException("Unknown operator in ${ctx.text} expression!")
                }
                return DecimalNode(value)
            } else throw IllegalArgumentException("RHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
        } else throw IllegalArgumentException("RHS ${ctx.lhs.text} not a number in ${ctx.text} expression!")
    }

    override fun visitNull(ctx: JSong2Parser.NullContext?): NullNode {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSong2Parser.NumberContext): DecimalNode {
        val number = ctx.NUMBER().text.toBigDecimal()
        return DecimalNode(
            when (ctx.SUB() != null) {
                true -> number.negate()
                else -> number
            }
        )
    }

    override fun visitObject(ctx: JSong2Parser.ObjectContext): ObjectNode {
        val obj = ObjectNode(nf)
        ctx.field().forEachIndexed { index, field ->
            val context = this.context
            val propertyName = reduce(visit(field.key))?.asText() ?: index.toString()
            this.context = context
            val value = reduce(visit(field.`val`)) ?: NullNode.instance
            this.context = context
            obj.set<JsonNode>(propertyName, value)
        }
        return obj
    }

    override fun visitParent(ctx: JSong2Parser.ParentContext): JsonNode? {
        return context?.let { back(it, ctx.MOD().size, parents) }
    }

    override fun visitRange(ctx: JSong2Parser.RangeContext): RangeNode {
        val min = visit(ctx.min)?.decimalValue() ?: BigDecimal.ZERO
        val max = visit(ctx.max)?.decimalValue() ?: BigDecimal.ZERO
        return RangeNode.between(min, max, nf)
    }

    override fun visitRegex(ctx: JSong2Parser.RegexContext): RegexNode {
        return RegexNode.of(ctx.text)
    }

    override fun visitRegexCI(ctx: JSong2Parser.RegexCIContext): RegexNode {
        return RegexNode.ci(ctx.text)
    }

    override fun visitRegexML(ctx: JSong2Parser.RegexMLContext): RegexNode {
        return RegexNode.ml(ctx.text)
    }


    override fun visitText(ctx: JSong2Parser.TextContext): TextNode {
        return TextNode(sanitise(ctx.STRING().text))
    }

    override fun visitTrue(ctx: JSong2Parser.TrueContext): BooleanNode {
        return BooleanNode.TRUE
    }

} //~ Processor