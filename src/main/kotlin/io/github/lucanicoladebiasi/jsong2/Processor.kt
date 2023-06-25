package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils

class Processor(
    val root: JsonNode?,
    val mapper: ObjectMapper
) : JSong2BaseVisitor<Results>() {

    companion object {

        fun sanitise(
            txt: String
        ): String {
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

    private val nf = mapper.nodeFactory

    override fun visitArray(ctx: JSong2Parser.ArrayContext): Results {
        val rs = Results(nf)
        ctx.element().forEach { element ->
            when{
                element.exp() != null -> rs.add(visit(element.exp()))
                element.range() != null-> rs.add(visit(element.range()))
            }
        }
        return rs
    }

    override fun visitFalse(ctx: JSong2Parser.FalseContext): Results {
        return Results(nf).add(BooleanNode.FALSE)
    }

    override fun visitJsong(ctx: JSong2Parser.JsongContext): Results {
        val rs = Results(nf)
        ctx.exp()?.let { exp ->
            rs.add(visit(exp).value)
        }
        return rs
    }

    override fun visitNull(ctx: JSong2Parser.NullContext?): Results {
        return Results(nf).add(NullNode.instance)
    }

    override fun visitNumber(ctx: JSong2Parser.NumberContext): Results {
        val number = ctx.NUMBER().text.toBigDecimal()
        return Results(nf).add(
            DecimalNode(
                when (ctx.MINUS() != null) {
                    true -> number.negate()
                    else -> number
                }
            )
        )
    }

    override fun visitObject(ctx: JSong2Parser.ObjectContext): Results {
        val obj = ObjectNode(nf)
        ctx.field().forEachIndexed() { index, field ->
            val propertyName = visit(field.key).value?.asText() ?: index.toString()
            val value = visit(field.`val`).value ?: NullNode.instance
            obj.set<JsonNode>(propertyName, value)
        }
        return Results(nf).add(obj)
    }

    override fun visitText(ctx: JSong2Parser.TextContext): Results {
        return Results(nf).add(TextNode(sanitise(ctx.STRING().text)))
    }

    override fun visitTrue(ctx: JSong2Parser.TrueContext): Results {
        return Results(nf).add(BooleanNode.TRUE)
    }

} //~ Processor