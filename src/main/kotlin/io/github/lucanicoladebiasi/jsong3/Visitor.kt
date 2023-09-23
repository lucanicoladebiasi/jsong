package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils

class Visitor(
    private val mapper: ObjectMapper
) : JSong2BaseVisitor<JsonNode?>() {

    companion object {

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

    override fun visitArray(ctx: JSong2Parser.ArrayContext): ArrayNode {
        val result = mapper.createArrayNode()
        ctx.element().forEach { element ->
            Visitor(mapper).visit(element)?.let { result.add(it) }
        }
        return result
    }


    override fun visitFalse(ctx: JSong2Parser.FalseContext): BooleanNode {
        return BooleanNode.FALSE
    }

    override fun visitJsong(ctx: JSong2Parser.JsongContext): JsonNode? {
        return ctx.exp()?.let { exp ->
            visit(exp)
        }
    }

    override fun visitNull(ctx: JSong2Parser.NullContext?): NullNode {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSong2Parser.NumberContext): DecimalNode {
        val value = ctx.NUMBER().text.toBigDecimal()
        return DecimalNode(when(ctx.SUB() == null) {
            true -> value
            else -> value.negate()
        })
    }

    override fun visitObject(ctx: JSong2Parser.ObjectContext): ObjectNode {
        val result = mapper.createObjectNode()
        ctx.field().forEachIndexed { index, field ->
            val key = Visitor(mapper).visit(field.key)?.asText() ?: index.toString()
            val value = Visitor(mapper).visit(field.`val`) ?: NullNode.instance
            result.set<JsonNode>(key, value)
        }
        return result
    }

    override fun visitText(ctx: JSong2Parser.TextContext): TextNode {
        return TextNode(sanitise(ctx.STRING().text))
    }

    override fun visitTrue(ctx: JSong2Parser.TrueContext?): BooleanNode {
        return BooleanNode.TRUE
    }

}//~ Visitor