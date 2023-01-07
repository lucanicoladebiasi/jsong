package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.RegexNode
import org.jsong.antlr.JSonicBaseVisitor
import org.jsong.antlr.JSonicLexer
import org.jsong.antlr.JSonicParser
import java.time.Instant
import kotlin.random.Random

class Processor(
    val root: JsonNode? = null,
    val om: ObjectMapper = ObjectMapper(),
    val random: Random = Random.Default,
    val time: Instant = Instant.now()
) : JSonicBaseVisitor<JsonNode?>() {

    companion object {

        private const val BACKTICK = '`'

        private fun normalizeFieldName(tag: String): String {
            return when {
                tag[0] == BACKTICK && tag[tag.length - 1] == BACKTICK -> tag.substring(1, tag.length - 1)
                else -> tag
            }
        }

    } //~ companion

    private var context: JsonNode? = null

    private var isToReduce: Boolean = true

    val lib: JSonataLFunctions = Library(this)

    val nf = om.nodeFactory

    init {
        context = root
    }

    private fun context(node: JsonNode?): JsonNode? {
        context = node
        return context
    }

    private fun descendants(node: JsonNode?): ArrayNode {
        val res = ArrayNode(nf)
        node?.fields()?.forEach { field ->
            if (field.value != null) {
                res.addAll(descendants(field.value))
                res.add(field.value)
            }
        }
        return res
    }


    fun evaluate(exp: String): JsonNode? {
        return visit(JSonicParser(CommonTokenStream(JSonicLexer(CharStreams.fromString(exp)))).jsong())
    }

    internal fun expand(node: JsonNode?): ArrayNode {
        return when (node) {
            null -> ArrayNode(nf)
            is ArrayNode -> node
            else -> ArrayNode(nf).add(node)
        }
    }

    private fun reduce(node: JsonNode?): JsonNode? {
        return if (isToReduce) when (node) {
            is ArrayNode -> when (node.size()) {
                0 -> null

                1 -> node[0]
                else -> node
            }

            else -> node
        } else node
    }

    override fun visitAdd(ctx: JSonicParser.AddContext): JsonNode? {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return context(DecimalNode(lhs.decimalValue().add(rhs.decimalValue())))
    }

    override fun visitAll(ctx: JSonicParser.AllContext): JsonNode? {
        val res = ArrayNode(nf)
        if (context is ObjectNode) {
            (context as ObjectNode).fields().forEach { field ->
                res.add(field.value)
            }
        }
        return context(reduce(res))
    }

    override fun visitArray(ctx: JSonicParser.ArrayContext): JsonNode? {
        val res = ArrayNode(nf)
        ctx.exp().forEach { exp ->
            res.add(visit(exp))
        }
        return context(res)
    }

    override fun visitBool(ctx: JSonicParser.BoolContext): JsonNode? {
        return context(
            when {
                ctx.FALSE() != null -> BooleanNode.FALSE
                ctx.TRUE() != null -> BooleanNode.TRUE
                else -> throw IllegalArgumentException("$ctx not recognized")
            }
        )
    }

    override fun visitConcatenate(ctx: JSonicParser.ConcatenateContext): JsonNode? {
        val sb = StringBuilder()
        visit(ctx.lhs)?.let { lhs -> sb.append(lhs) }
        visit(ctx.rhs)?.let { rhs -> sb.append(rhs) }
        return context(TextNode(sb.toString()))
    }

    override fun visitContext(ctx: JSonicParser.ContextContext): JsonNode? {
        return context
    }

    override fun visitDescendants(ctx: JSonicParser.DescendantsContext): JsonNode? {
        val res = ArrayNode(nf)
        if (context is ObjectNode) {
            res.addAll(descendants(context))
        }
        return context(reduce(res))
    }

    override fun visitDiv(ctx: JSonicParser.DivContext): JsonNode? {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return context(DecimalNode(lhs.decimalValue().divide(rhs.decimalValue())))
    }

    override fun visitEq(ctx: JSonicParser.EqContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(lhs == rhs)
        return context(res)
    }

    override fun visitExpand(ctx: JSonicParser.ExpandContext): JsonNode? {
        val res = expand(visit(ctx.exp()))
        isToReduce = false
        return context(res)
    }

    override fun visitField(ctx: JSonicParser.FieldContext): JsonNode? {
        val res = when (context) {
            is ObjectNode -> {
                val node = context as ObjectNode
                val field = normalizeFieldName(ctx.text)
                when (node.has(field)) {
                    true -> node[field]
                    else -> null
                }
            }

            else -> null
        }
        return context(res)
    }

    override fun visitGt(ctx: JSonicParser.GtContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(when {
            lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() > rhs.decimalValue()
            else -> lib.string(lhs).textValue() > lib.string(rhs).textValue()
        })
        return context(res)
    }

    override fun visitGte(ctx: JSonicParser.GteContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(when {
            lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() >= rhs.decimalValue()
            else -> lib.string(lhs).textValue() >= lib.string(rhs).textValue()
        })
        return context(res)
    }

    override fun visitFilter(ctx: JSonicParser.FilterContext): JsonNode? {
        val res = ArrayNode(nf)
        val lhs = expand(visit(ctx.lhs))
        lhs.forEachIndexed { index, node ->
            context(node)
            when (val rhs = visit(ctx.rhs)) {
                is NumericNode -> {
                    val value = rhs.asInt()
                    val offset = if (value < 0) lhs.size() + value else value
                    if (index == offset) {
                        res.add(node)
                    }
                }

                is RangesNode -> {
                    if (rhs.indexes.map { it.asInt() }.contains(index)) {
                        res.add(node)
                    }
                }

                else -> {
                    val predicate = rhs?.asBoolean() ?: false
                    if (predicate) {
                        res.add(node)
                    }
                }

            }
        }
        return reduce(res)
    }



    override fun visitJsong(ctx: JSonicParser.JsongContext): JsonNode? {
        var res: JsonNode? = null
        ctx.exp().forEach { exp ->
            res = visit(exp)
        }
        return res
    }

    override fun visitLt(ctx: JSonicParser.LtContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(when {
            lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() < rhs.decimalValue()
            else -> lib.string(lhs).textValue() < lib.string(rhs).textValue()
        })
        return context(res)
    }

    override fun visitLte(ctx: JSonicParser.LteContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(when {
            lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() <= rhs.decimalValue()
            else -> lib.string(lhs).textValue() <= lib.string(rhs).textValue()
        })
        return context(res)
    }

    override fun visitMap(ctx: JSonicParser.MapContext): JsonNode? {
        val res = ArrayNode(nf)
        expand(visit(ctx.lhs)).forEach { lhs ->
            context(lhs)
            when (val rhs = visit(ctx.rhs)) {
                is ArrayNode -> res.addAll(rhs)
                else -> rhs?.let { res.add(it) }
            }
        }
        return context(reduce(res))
    }

    override fun visitMod(ctx: JSonicParser.ModContext): JsonNode? {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return context(DecimalNode(lhs.decimalValue().remainder(rhs.decimalValue())))
    }

    override fun visitMul(ctx: JSonicParser.MulContext): JsonNode? {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return context(DecimalNode(lhs.decimalValue().multiply(rhs.decimalValue())))
    }

    override fun visitNe(ctx: JSonicParser.NeContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(lhs != rhs)
        return context(res)
    }

    override fun visitNihil(ctx: JSonicParser.NihilContext): JsonNode? {
        return context(NullNode.instance)
    }

    override fun visitNumber(ctx: JSonicParser.NumberContext): JsonNode? {
        return context(DecimalNode(ctx.text.toBigDecimal()))
    }

    override fun visitObj(ctx: JSonicParser.ObjContext): JsonNode? {
        val res = ObjectNode(nf)
        ctx.pair().forEachIndexed { index, pair ->
            val key = visit(pair.key)?.asText() ?: index.toString()
            val value = visit(pair.value) ?: NullNode.instance
            res.set<JsonNode>(key, value)
        }
        return context(res)
    }

    override fun visitRange(ctx: JSonicParser.RangeContext): JsonNode? {
        return context(
            RangeNode.of(
                ctx.min.text.toBigDecimal(),
                ctx.max.text.toBigDecimal(),
                nf
            )
        )
    }

    override fun visitRanges(ctx: JSonicParser.RangesContext): JsonNode? {
        val res = RangesNode(nf)
        ctx.range().forEach {
            res.add(visit(it))
        }
        return context(res)
    }

    override fun visitRegex(ctx: JSonicParser.RegexContext): JsonNode? {
        return context(RegexNode(ctx.REGEX().text))
    }

    override fun visitRoot(ctx: JSonicParser.RootContext): JsonNode? {
        return context(root)
    }

    override fun visitScope(ctx: JSonicParser.ScopeContext): JsonNode? {
        ctx.exp().forEach { exp ->
            context(visit(exp))
        }
        return context
    }

    override fun visitSub(ctx: JSonicParser.SubContext): JsonNode? {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return context(DecimalNode(lhs.decimalValue().subtract(rhs.decimalValue())))
    }

    override fun visitText(ctx: JSonicParser.TextContext): JsonNode? {
        return context(TextNode(ctx.text.substring(1, ctx.text.length - 1)))
    }

}

