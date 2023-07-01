package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils
import java.math.BigDecimal
import java.util.Deque
import kotlin.math.sign

class Processor(
    root: JsonNode?,
    mapper: ObjectMapper
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

    private var context = Context(root ?: NullNode.instance)

    private var index: Int? = null

    override fun visitArray(ctx: JSong2Parser.ArrayContext): ResultSequence {
        val array = ArrayNode(nf)
        ctx.element().forEach { element ->
            when {
                element.exp() != null -> array.add(visit(element.exp()).value(nf))
                element.range() != null -> array.add(visit(element.range()).value(nf))
            }
        }
        return ResultSequence(Context(array))
    }

    override fun visitFalse(ctx: JSong2Parser.FalseContext): ResultSequence {
        return ResultSequence(Context(BooleanNode.FALSE))
    }

    override fun visitFilter(ctx: JSong2Parser.FilterContext): ResultSequence {
        val rs = ResultSequence()
        val lhs = visit(ctx.lhs)
        lhs.forEach { context ->
            this.context = context
            val rhs = visit(ctx.rhs)
            val predicate = rhs.value(nf)
            if (predicate != null) {
                val indexes = RangeNode.indexes(predicate)
                if (indexes.isNotEmpty()) {
                    indexes.forEach { index ->
                        this.context[index]?.let { rs.add(it) }
                    }
                }
            }
        }
        return rs
    }

    override fun visitId(ctx: JSong2Parser.IdContext): ResultSequence {
        val fieldName = sanitise(ctx.ID().text)
        val rs = ResultSequence()
        when(context.node) {
            is ArrayNode -> context.node.filter { node ->
                node.isObject && node.has(fieldName)
            }.forEach { node ->
                rs.add(Context(node[fieldName], context))
            }
            is ObjectNode -> if (context.node.has(fieldName)) {
                rs.add(Context(context.node[fieldName], context))
            }
        }
        return rs
    }

    override fun visitJsong(ctx: JSong2Parser.JsongContext): ResultSequence {
        val rs = ResultSequence()
        ctx.exp()?.let { exp ->
            rs.add(visit(exp))
        }
        return rs
    }

    override fun visitMap(ctx: JSong2Parser.MapContext): ResultSequence {
        val rs = ResultSequence()
        val lhs = visit(ctx.lhs)
        lhs.forEachIndexed { index, context ->
            this.index = index
            this.context = context
            val rhs = visit(ctx.rhs)
            rs.add(rhs)
        }
        this.index = null
        return rs
    }

    override fun visitNull(ctx: JSong2Parser.NullContext?): ResultSequence {
        return ResultSequence(Context(NullNode.instance))
    }

    override fun visitNumber(ctx: JSong2Parser.NumberContext): ResultSequence {
        val number = ctx.NUMBER().text.toBigDecimal()
        return ResultSequence(
            Context(
                DecimalNode(
                    when (ctx.SUB() != null) {
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
        return ResultSequence(Context(obj))
    }

    override fun visitParent(ctx: JSong2Parser.ParentContext): ResultSequence {
        val rs = ResultSequence()
        val steps = ctx.MOD().size
        when(context.node) {
            is ArrayNode -> repeat(context.node.size()) {
                context.back(steps)?.let { rs.add(it) }
            }
            else -> context.back(steps)?.let { rs.add(it) }
        }
        return rs
    }

    override fun visitRange(ctx: JSong2Parser.RangeContext): ResultSequence {
        val min = visit(ctx.min).value(nf)?.decimalValue() ?: BigDecimal.ZERO
        val max = visit(ctx.max).value(nf)?.decimalValue() ?: BigDecimal.ZERO
        return ResultSequence(Context(RangeNode.between(min, max, nf)))
    }

    override fun visitRegex(ctx: JSong2Parser.RegexContext): ResultSequence {
        return ResultSequence(Context(RegexNode.of(ctx.text)))
    }

    override fun visitRegexCI(ctx: JSong2Parser.RegexCIContext): ResultSequence {
        return ResultSequence(Context(RegexNode.ci(ctx.text)))
    }

    override fun visitRegexML(ctx: JSong2Parser.RegexMLContext): ResultSequence {
        return ResultSequence(Context(RegexNode.ml(ctx.text)))
    }

    override fun visitText(ctx: JSong2Parser.TextContext): ResultSequence {
        return ResultSequence(Context(TextNode(sanitise(ctx.STRING().text))))
    }

    override fun visitTrue(ctx: JSong2Parser.TrueContext): ResultSequence {
        return ResultSequence(Context(BooleanNode.TRUE))
    }

} //~ Processor