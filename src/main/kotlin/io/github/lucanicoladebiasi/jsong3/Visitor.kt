package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.apache.commons.text.StringEscapeUtils
import java.math.BigDecimal

class Visitor(
    private val context: JsonNode?,
    private val mapper: ObjectMapper
) : JSong2BaseVisitor<JsonNode?>() {

    companion object {

        private fun expand(node: JsonNode?, mapper: ObjectMapper): ArrayNode {
            val result = mapper.createArrayNode()
            if (node != null) when (node) {
                is ArrayNode -> result.addAll(node)
                else -> result.add(node)
            }
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


    override fun visit(tree: ParseTree?): JsonNode? {
        return reduce(super.visit(tree))
    }

    override fun visitArray(ctx: JSong2Parser.ArrayContext): ArrayNode {
        val result = mapper.createArrayNode()
        ctx.element().forEach { element ->
            Visitor(context, mapper).visit(element)?.let { result.add(it) }
        }
        return result
    }

    override fun visitBlock(ctx: JSong2Parser.BlockContext): JsonNode? {
        var result: JsonNode? = null
        ctx.exp().forEach { exp ->
            result = Visitor(context, mapper).visit(exp)
        }
        return result
    }

    override fun visitContext(ctx: JSong2Parser.ContextContext): JsonNode? {
        return context
    }

    override fun visitFilter(ctx: JSong2Parser.FilterContext): ArrayNode {
        val result = mapper.createArrayNode()
        val lhs = expand(Visitor(context, mapper).visit(ctx.lhs), mapper)
        lhs.forEachIndexed { index, context ->
            val rhs = Visitor(context, mapper).visit(ctx.rhs)
            val indexes = index(rhs, lhs.size(), mapper)
            if (indexes.isNotEmpty()) {
                if (indexes.contains(index)) {
                    result.add(context)
                }
            } else {
                TODO()
            }
        }
        return result
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

    override fun visitJsong(ctx: JSong2Parser.JsongContext): JsonNode? {
        return ctx.exp()?.let { exp ->
            visit(exp)
        }
    }

    override fun visitMap(ctx: JSong2Parser.MapContext): ArrayNode {
        val result = mapper.createArrayNode()
        val lhs = expand(Visitor(context, mapper).visit(ctx.lhs), mapper)
        lhs.forEach { context ->
            val rhs = Visitor(context, mapper).visit(ctx.rhs)
            if (rhs != null) when (rhs) {
                is ArrayNode -> result.addAll(rhs)
                else -> result.add(rhs)
            }
        }
        return result
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
            val key = Visitor(context, mapper).visit(field.key)?.asText() ?: index.toString()
            val value = Visitor(context, mapper).visit(field.`val`) ?: NullNode.instance
            result.set<JsonNode>(key, value)
        }
        return result
    }

    override fun visitRange(ctx: JSong2Parser.RangeContext): RangeNode {
        return RangeNode.between(
            Visitor(context, mapper).visit(ctx.min)?.decimalValue() ?: BigDecimal.ZERO,
            Visitor(context, mapper).visit(ctx.max)?.decimalValue() ?: BigDecimal.ZERO,
            mapper
        )
    }

    override fun visitText(ctx: JSong2Parser.TextContext): TextNode {
        return TextNode(sanitise(ctx.STRING().text))
    }

    override fun visitTrue(ctx: JSong2Parser.TrueContext?): BooleanNode {
        return BooleanNode.TRUE
    }

}//~ Visitor