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
) : JSong2BaseVisitor<ResultNode>() {

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

    override fun visitArray(ctx: JSong2Parser.ArrayContext): ResultNode {
        val rs = ResultNode(nf)
        ctx.element().forEach { element ->
            when{
                element.exp() != null -> rs.add(visit(element.exp()))
                element.range() != null-> rs.add(visit(element.range()))
            }
        }
        return rs
    }

    override fun visitFalse(ctx: JSong2Parser.FalseContext): ResultNode {
        return ResultNode(nf).add(BooleanNode.FALSE)
    }

    override fun visitJsong(ctx: JSong2Parser.JsongContext): ResultNode {
        val rs = ResultNode(nf)
        ctx.exp()?.let { exp ->
            rs.add(visit(exp).value)
        }
        return rs
    }

    override fun visitNull(ctx: JSong2Parser.NullContext?): ResultNode {
        return ResultNode(nf).add(NullNode.instance)
    }

    override fun visitNumber(ctx: JSong2Parser.NumberContext): ResultNode {
        val number = ctx.NUMBER().text.toBigDecimal()
        return ResultNode(nf).add(
            DecimalNode(
                when (ctx.MINUS() != null) {
                    true -> number.negate()
                    else -> number
                }
            )
        )
    }

    override fun visitObject(ctx: JSong2Parser.ObjectContext): ResultNode {
        val obj = ObjectNode(nf)
        ctx.field().forEachIndexed() { index, field ->
            val propertyName = visit(field.key).value?.asText() ?: index.toString()
            val value = visit(field.`val`).value ?: NullNode.instance
            obj.set<JsonNode>(propertyName, value)
        }
        return ResultNode(nf).add(obj)
    }

    override fun visitRange(ctx: JSong2Parser.RangeContext): ResultNode {
        return ResultNode(nf).add(
            RangeNode.between(visit(ctx.min).value!!.decimalValue() ,visit(ctx.max).value!!.decimalValue(), nf)
        )
    }

    override fun visitText(ctx: JSong2Parser.TextContext): ResultNode {
        return ResultNode(nf).add(TextNode(sanitise(ctx.STRING().text)))
    }

    override fun visitTrue(ctx: JSong2Parser.TrueContext): ResultNode {
        return ResultNode(nf).add(BooleanNode.TRUE)
    }

} //~ Processor