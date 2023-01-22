package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.antlr.JSonicBaseVisitor
import org.jsong.antlr.JSonicLexer
import org.jsong.antlr.JSonicParser
import java.lang.reflect.InvocationTargetException
import java.math.MathContext
import java.time.Instant
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions

class Processor(
    val root: JsonNode? = null,
    val mc: MathContext = MathContext.DECIMAL128,
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

    //private val contextStack = ArrayDeque<JsonNode>()

    private var context = root

    private var isToReduce: Boolean = true

    val lib: JSonataLFunctions = Library(this)

    val nf: JsonNodeFactory = om.nodeFactory

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
            is RangeNode -> RangesNode(nf).add(node)
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

    override fun visitAdd(ctx: JSonicParser.AddContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().add(rhs.decimalValue()))
    }

    override fun visitAll(ctx: JSonicParser.AllContext): JsonNode? {
        val res = ArrayNode(nf)
        if (context is ObjectNode) {
            context?.fields()?.forEach { field ->
                res.add(field.value)
            }
        }
        return reduce(res)
    }

    override fun visitAnd(ctx: JSonicParser.AndContext): JsonNode? {
        val lhs = lib.boolean(visit(ctx.lhs))
        val rhs = lib.boolean(visit(ctx.rhs))
        return BooleanNode.valueOf(lhs.booleanValue() && rhs.booleanValue())
    }

    override fun visitArray(ctx: JSonicParser.ArrayContext): JsonNode {
        val res = ArrayNode(nf)
        ctx.exp().forEach { exp ->
            res.add(visit(exp))
        }
        return res
    }

    override fun visitBool(ctx: JSonicParser.BoolContext): JsonNode? {
        return when {
            ctx.FALSE() != null -> BooleanNode.FALSE
            ctx.TRUE() != null -> BooleanNode.TRUE
            else -> throw IllegalArgumentException("$ctx not recognized")
        }

    }

    private fun recall(type: KClass<*>, name: String, args: List<Any?>): KFunction<*> {
        type.memberFunctions.filter { it.name == name }.forEach { function ->
            if (function.parameters.size >= args.size - 1) {
                return function
            }
        }
        throw IllegalArgumentException("Function $args) not found.")
    }

    override fun visitCall(ctx: JSonicParser.CallContext): JsonNode? {
        val args = mutableListOf<Any?>()
        args.add(lib)
        val context = this.context
        when (ctx.exp().isEmpty()) {
            true -> args.add(context)
            else -> ctx.exp().forEach { exp ->
                this.context = context
                args.add(visit(exp))
            }
        }
        val function = recall(lib::class, ctx.label().text, args)
        when {
            args.size > function.parameters.size -> while (args.size > function.parameters.size) {
                args.removeLast()
            }
            args.size < function.parameters.size -> while (args.size < function.parameters.size) {
                args.add(null)
            }
        }
        try {
            return function.call(*args.toTypedArray()) as JsonNode?
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }

    override fun visitConcatenate(ctx: JSonicParser.ConcatenateContext): JsonNode {
        val sb = StringBuilder()
        visit(ctx.lhs)?.let { lhs ->
            sb.append(lib.string(lhs).textValue())
        }
        visit(ctx.rhs)?.let { rhs ->
            sb.append(lib.string(rhs).textValue())
        }
        return TextNode(sb.toString())
    }

    override fun visitCondition(ctx: JSonicParser.ConditionContext): JsonNode? {
        return when (lib.boolean(visit(ctx.prd)).booleanValue()) {
            true -> visit(ctx.pos)
            else -> visit(ctx.neg)
        }
    }

    override fun visitContext(ctx: JSonicParser.ContextContext): JsonNode? {
        return context
    }

    override fun visitDescendants(ctx: JSonicParser.DescendantsContext): JsonNode? {
        val res = ArrayNode(nf)
        if (context is ObjectNode) {
            res.addAll(descendants(context))
        }
        return reduce(res)
    }

    override fun visitDiv(ctx: JSonicParser.DivContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().divide(rhs.decimalValue(), mc))
    }

    override fun visitEq(ctx: JSonicParser.EqContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        return BooleanNode.valueOf(lhs == rhs)
    }

    override fun visitExpand(ctx: JSonicParser.ExpandContext): JsonNode {
        val res = expand(visit(ctx.exp()))
        isToReduce = false
        return res
    }

    override fun visitField(ctx: JSonicParser.FieldContext): JsonNode? {
        val res = when (context) {
            is ObjectNode -> {
                val field = normalizeFieldName(ctx.text)
                when (context?.has(field)) {
                    true -> context?.get(field)
                    else -> null
                }
            }

            else -> null
        }
        return res
    }

    override fun visitFilter(ctx: JSonicParser.FilterContext): JsonNode? {
        val res = ArrayNode(nf)
        val lhs = expand(visit(ctx.lhs))
        lhs.forEachIndexed { index, context ->
            this.context = context
            when (val rhs = visit(ctx.rhs)) {
                is NumericNode -> {
                    val value = rhs.asInt()
                    val offset = if (value < 0) lhs.size() + value else value
                    if (index == offset) {
                        res.add(context)
                    }
                }

                is RangesNode -> {
                    if (rhs.indexes.map { it.asInt() }.contains(index)) {
                        res.add(context)
                    }
                }

                else -> {
                    val predicate = lib.boolean(rhs).asBoolean()
                    if (predicate) {
                        res.add(context)
                    }
                }

            }
        }
        return reduce(res)
    }

    override fun visitGt(ctx: JSonicParser.GtContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(
            when {
                lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() > rhs.decimalValue()
                else -> lib.string(lhs).textValue() > lib.string(rhs).textValue()
            }
        )
        return res
    }

    override fun visitGte(ctx: JSonicParser.GteContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(
            when {
                lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() >= rhs.decimalValue()
                else -> lib.string(lhs).textValue() >= lib.string(rhs).textValue()
            }
        )
        return res
    }

    override fun visitIn(ctx: JSonicParser.InContext): JsonNode? {
        val lhs = reduce(visit(ctx.lhs))
        val rhs = expand(visit(ctx.rhs))
        return BooleanNode.valueOf(rhs.contains(lhs))
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
        val res = BooleanNode.valueOf(
            when {
                lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() < rhs.decimalValue()
                else -> lib.string(lhs).textValue() < lib.string(rhs).textValue()
            }
        )
        return res
    }

    override fun visitLte(ctx: JSonicParser.LteContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val res = BooleanNode.valueOf(
            when {
                lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() <= rhs.decimalValue()
                else -> lib.string(lhs).textValue() <= lib.string(rhs).textValue()
            }
        )
        return res
    }

    override fun visitMap(ctx: JSonicParser.MapContext): JsonNode? {
        val res = ArrayNode(nf)
        when (val lhs = expand(visit(ctx.lhs))) {
            is RangesNode -> lhs.indexes.forEach { context ->
                this.context = context
                when (val rhs = visit(ctx.rhs)) {
                    is ArrayNode -> res.addAll(rhs)
                    else -> rhs?.let { res.add(it) }
                }
            }

            else -> lhs.forEach { context ->
                this.context = context
                when (val rhs = visit(ctx.rhs)) {
                    is ArrayNode -> res.addAll(rhs)
                    else -> rhs?.let { res.add(it) }
                }
            }
        }
        return reduce(res)
    }

    override fun visitMod(ctx: JSonicParser.ModContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().remainder(rhs.decimalValue()))
    }

    override fun visitMul(ctx: JSonicParser.MulContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().multiply(rhs.decimalValue()))
    }

    override fun visitNe(ctx: JSonicParser.NeContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        return BooleanNode.valueOf(lhs != rhs)
    }

    override fun visitNihil(ctx: JSonicParser.NihilContext): JsonNode? {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSonicParser.NumberContext): JsonNode {
        return DecimalNode(ctx.text.toBigDecimal())
    }

    override fun visitObj(ctx: JSonicParser.ObjContext): JsonNode {
        val res = ObjectNode(nf)
        ctx.pair().forEachIndexed { index, pair ->
            val key = visit(pair.key)?.asText() ?: index.toString()
            val value = visit(pair.value) ?: NullNode.instance
            res.set<JsonNode>(key, value)
        }
        return res
    }

    override fun visitOr(ctx: JSonicParser.OrContext): JsonNode? {
        val lhs = lib.boolean(visit(ctx.lhs))
        val rhs = lib.boolean(visit(ctx.rhs))
        return BooleanNode.valueOf(lhs.booleanValue() || rhs.booleanValue())
    }

    override fun visitRange(ctx: JSonicParser.RangeContext): JsonNode {
        val min = visit(ctx.min)
        val max = visit(ctx.max)
        return RangeNode.of(
            when(min) {
                is DecimalNode -> min.decimalValue()
                else -> lib.string(min).textValue().toBigDecimal()
            },
            when(max) {
                is DecimalNode -> max.decimalValue()
                else -> lib.string(max).textValue().toBigDecimal()
            },
            nf
        )
    }

    override fun visitRanges(ctx: JSonicParser.RangesContext): JsonNode {
        val res = RangesNode(nf)
        ctx.range().forEach {
            res.add(visit(it))
        }
        return res
    }

    override fun visitRegex(ctx: JSonicParser.RegexContext): JsonNode {
        return RegexNode(ctx.REGEX().text)
    }

    override fun visitRoot(ctx: JSonicParser.RootContext): JsonNode? {
        return root
    }

    override fun visitScope(ctx: JSonicParser.ScopeContext): JsonNode? {
        ctx.exp().forEach { exp ->
            context = visit(exp)
        }
        return context
    }

    override fun visitSub(ctx: JSonicParser.SubContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().subtract(rhs.decimalValue()))
    }

    override fun visitText(ctx: JSonicParser.TextContext): JsonNode {
        return TextNode(ctx.text.substring(1, ctx.text.length - 1))
    }

}

