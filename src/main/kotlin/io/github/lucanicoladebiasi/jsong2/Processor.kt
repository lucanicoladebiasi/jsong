package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils
import java.math.BigDecimal

class Processor(
    val root: JsonNode?,
    val mapper: ObjectMapper
) : JSong2BaseVisitor<ResultSequence>() {

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

    override fun visitArray(ctx: JSong2Parser.ArrayContext): ResultSequence {
        val rs = ResultSequence()
        ctx.element().forEach { element ->
            when {
                element.exp() != null -> rs.add(visit(element.exp()))
                element.range() != null -> rs.add(visit(element.range()))
            }
        }
        return rs
    }

    override fun visitFalse(ctx: JSong2Parser.FalseContext): ResultSequence {
        return ResultSequence().add(Context(BooleanNode.FALSE))
    }

    override fun visitId(ctx: JSong2Parser.IdContext): ResultSequence {
        return super.visitId(ctx)
    }

    override fun visitJsong(ctx: JSong2Parser.JsongContext): ResultSequence {
        val rs = ResultSequence()
        ctx.exp()?.let { exp ->
            rs.add(visit(exp))
        }
        return rs
    }

    override fun visitNull(ctx: JSong2Parser.NullContext?): ResultSequence {
        return ResultSequence().add(Context(NullNode.instance))
    }

    override fun visitNumber(ctx: JSong2Parser.NumberContext): ResultSequence {
        val number = ctx.NUMBER().text.toBigDecimal()
        return ResultSequence().add(
            Context(
                DecimalNode(
                    when (ctx.MINUS() != null) {
                        true -> number.negate()
                        else -> number
                    }
                )
            )
        )
    }

    override fun visitObject(ctx: JSong2Parser.ObjectContext): ResultSequence {
        val obj = ObjectNode(nf)
        ctx.field().forEachIndexed() { index, field ->
            val propertyName = visit(field.key).value(nf)?.asText() ?: index.toString()
            val value = visit(field.`val`).value(nf) ?: NullNode.instance
            obj.set<JsonNode>(propertyName, value)
        }
        return ResultSequence().add(Context(obj))
    }

    override fun visitRange(ctx: JSong2Parser.RangeContext): ResultSequence {
        val min = visit(ctx.min).value(nf)?.decimalValue() ?: BigDecimal.ZERO
        val max = visit(ctx.max).value(nf)?.decimalValue() ?: BigDecimal.ZERO
        return ResultSequence().add(Context(RangeNode.between(min, max, nf)))
    }

    override fun visitRegex(ctx: JSong2Parser.RegexContext): ResultSequence {
        return ResultSequence().add(Context(RegexNode.of(ctx.text)))
    }

    override fun visitRegexci(ctx: JSong2Parser.RegexciContext): ResultSequence {
        return ResultSequence().add(Context(RegexNode.ci(ctx.text)))
    }

    override fun visitRegexml(ctx: JSong2Parser.RegexmlContext): ResultSequence {
        return ResultSequence().add(Context(RegexNode.ml(ctx.text)))
    }

    override fun visitText(ctx: JSong2Parser.TextContext): ResultSequence {
        return ResultSequence().add(Context(TextNode(sanitise(ctx.STRING().text))))
    }

    override fun visitTrue(ctx: JSong2Parser.TrueContext): ResultSequence {
        return ResultSequence().add(Context(BooleanNode.TRUE))
    }

} //~ Processor