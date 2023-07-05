package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils
import java.math.BigDecimal

class Processor(
    root: JsonNode?,
    mapper: ObjectMapper
) : JSong2BaseVisitor<JsonNode?>() {

    companion object {

        fun expand(nf: JsonNodeFactory, node: JsonNode?): ArrayNode {
            val array = ArrayNode(nf)
            if (node != null) when (node) {
                is ArrayNode -> array.addAll(node)
                else -> array.add(node)
            }
            return array
        }

        fun reduce(node: JsonNode?): JsonNode? {
            return when (node) {
                is ArrayNode -> when (node.size()) {
                    0 -> null
                    1 -> node[0]
                    else -> node
                }

                else -> node
            }
        }

        fun sanitise(txt: String): String {
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

        private fun select(array: ArrayNode, index: Int): JsonNode? {
            val offset = if (index < 0) array.size() + index else index
            return if (offset in 0 until array.size()) {
                array[offset]
            } else null
        }

    } //~ companion

    private var context: JsonNode? = root

    private val nf = mapper.nodeFactory

    private val parents = mutableMapOf<JsonNode, JsonNode>()

    private fun back(node: JsonNode, step: Int): JsonNode? {
        return when (step) {
            0 -> {
                node
            }

            else -> {
                parents[node]?.let { back(it, step - 1) }
            }
        }
    }

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

    override fun visitFalse(ctx: JSong2Parser.FalseContext): BooleanNode {
        return BooleanNode.FALSE
    }

    override fun visitFilter(ctx: JSong2Parser.FilterContext): ArrayNode {
        val array = ArrayNode(nf)
        val lhs = expand(nf, visit(ctx.lhs))
        val rhs = visit(ctx.rhs)
        val indexes = RangeNode.indexes(rhs)
        if (indexes.isNotEmpty()) {
            indexes.forEach { index ->
                select(lhs, index)?.let { array.add(it) }
            }
        }
        return array
    }

    override fun visitId(ctx: JSong2Parser.IdContext): JsonNode? {
        val fieldName = sanitise(ctx.ID().text)
        val context = this.context
        return if (context is ObjectNode && context.has(fieldName)) {
            return context[fieldName]
        } else null
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
        val lhs = expand(nf, visit(ctx.lhs))
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
        return context?.let { back(it, ctx.MOD().size) }
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