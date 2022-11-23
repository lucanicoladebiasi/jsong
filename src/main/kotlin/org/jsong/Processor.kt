package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.jsong.antlr.JSongBaseVisitor
import org.jsong.antlr.JSongParser
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import kotlin.random.Random

class Processor internal constructor(
    private val mapper: ObjectMapper,
    private val random: Random,
    private val time: Instant,
    private val root: JsonNode?
) : JSongBaseVisitor<JsonNode?>() {

    private val context = ArrayDeque<JsonNode>()

    private val contextual = mutableMapOf<String, ArrayNode>()

    private val functions = Functions(mapper, random, time)

    @Volatile
    private var isToFlatten = true

    private val loop = ArrayDeque<Int>()

    private val positional = mutableMapOf<String, ArrayNode>()

    private val scope = ArrayDeque<JsonNode>() // todo: apply to maps?

    private val stack = ArrayDeque<ArrayNode>()

    private val variables = mutableMapOf<String, VarNode>()

    init {
        push(root)
    }

    private fun descendants(node: JsonNode?): ArrayNode {
        val exp = mapper.createArrayNode()
        functions.array(node).forEach { element ->
            element.fields().forEach { property ->
                if (property.value != null) {
                    exp.addAll(descendants(property.value).toList())
                    exp.add(property.value)
                }
            }
        }
        return exp
    }

    private fun push(node: JsonNode?): JsonNode? {
        stack.push(functions.array(functions.flatten(node)))
        return node
    }

    private fun pop(): ArrayNode {
        return when (stack.isEmpty()) {
            true -> mapper.createArrayNode()
            else -> stack.pop()
        }
    }

    private fun select(node: JsonNode?, path: JsonNode?): ArrayNode {
        val exp = mapper.createArrayNode()
        when (path) {
            is PathNode -> when (node) {
                is ArrayNode -> node.forEach {
                    exp.addAll(select(it, path))
                }

                is ObjectNode -> node[path.asText()]?.let {
                    when (it) {
                        is ArrayNode -> exp.addAll(it)
                        else -> exp.add(it)
                    }
                }
            }
        }
        return exp
    }

    private fun stretch(arr: ArrayNode, size: Int): ArrayNode {
        val exp = mapper.createArrayNode()
        val mul = size / arr.size()
        arr.forEach { e ->
            for (i in 0 until mul) {
                exp.add(e)
            }
        }
        return exp
    }

    override fun visitAdd(ctx: JSongParser.AddContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.add(lhs, rhs))
    }

    override fun visitAnd(ctx: JSongParser.AndContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.and(functions.flatten(lhs), functions.flatten(rhs)))
    }

    override fun visitArray(ctx: JSongParser.ArrayContext): JsonNode? {
        val exp = mapper.createArrayNode()
        ctx.children.forEach { child ->
            visit(child)
            pop().let { exp.add(it) }
        }
        return push(exp)
    }

    override fun visitArrayFunction(ctx: JSongParser.ArrayFunctionContext): JsonNode? {
        val args = mutableListOf<JsonNode>()
        ctx.exp().forEach { arg ->
            visit(arg)
            pop().let { args.add(it) }
        }
        val fnc = ctx.array_fun()
        val exp = when {
            fnc.APPEND() != null -> when (args.size) {
                1 -> functions.append(pop(), args[0])
                2 -> functions.append(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.APPEND}")
            }

            fnc.COUNT() != null -> when (args.size) {
                0 -> functions.count(pop())
                1 -> functions.count(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.COUNT}")
            }

            fnc.DISTINCT() != null -> mapper.createArrayNode().addAll(
                when (args.size) {
                    0 -> functions.distinct(pop())
                    1 -> functions.distinct(args[0])
                    else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.DISTINCT}")
                }
            )

            fnc.REVERSE() != null -> when (args.size) {
                0 -> functions.reverse(pop())
                1 -> functions.reverse(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.REVERSE}")
            }

            fnc.SHUFFLE() != null -> when (args.size) {
                0 -> functions.shuffle(pop(), random)
                1 -> functions.shuffle(args[0], random)
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SHUFFLE}")
            }

            fnc.SORT() != null -> when (args.size) {
                0 -> functions.sort(pop())
                1 -> functions.sort(args[0])
                2 -> functions.sort(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SORT}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)
    }

    override fun visitArrayConstructor(ctx: JSongParser.ArrayConstructorContext): JsonNode? {
        visit(ctx.exp())
        isToFlatten = false
        return stack.peek()
    }

    override fun visitBool(ctx: JSongParser.BoolContext): JsonNode? {
        return push(
            when {
                ctx.FALSE() != null -> BooleanNode.FALSE
                ctx.TRUE() != null -> BooleanNode.TRUE
                else -> throw IllegalArgumentException("$ctx not recognized")
            }
        )
    }

    override fun visitBooleanFunction(ctx: JSongParser.BooleanFunctionContext): JsonNode? {
        val args = mutableListOf<JsonNode>()
        ctx.exp().forEach { arg ->
            visit(arg)
            pop().let { args.add(it) }
        }
        val fnc = ctx.bool_fun()
        val exp = when {
            fnc.BOOLEAN() != null -> when (args.size) {
                0 -> functions.boolean(pop())
                1 -> functions.boolean(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.BOOLEAN}")
            }

            fnc.EXISTS() != null -> when (args.size) {
                0 -> functions.exists(pop())
                1 -> functions.exists(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.EXISTS}")
            }

            fnc.NOT() != null -> when (args.size) {
                0 -> functions.not(pop())
                1 -> functions.not(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.NOT}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)
    }

    override fun visitConcatenate(ctx: JSongParser.ConcatenateContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.concatenate(functions.flatten(lhs), functions.flatten(rhs)))
    }

    override fun visitContext(ctx: JSongParser.ContextContext): JsonNode? {
        return push(stack.firstOrNull())
    }

    override fun visitDefineFunction(ctx: JSongParser.DefineFunctionContext): JsonNode? {
        ctx.label().forEach { label -> }

        return null
    }

    override fun visitDescendants(ctx: JSongParser.DescendantsContext): JsonNode? {
        return push(descendants(pop()))
    }

    override fun visitDiv(ctx: JSongParser.DivContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.div(lhs, rhs))
    }

    override fun visitEq(ctx: JSongParser.EqContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        val res = functions.eq(lhs, rhs)
        return push(res)
    }

    override fun visitFilter(ctx: JSongParser.FilterContext): JsonNode? {
        val exp = mapper.createArrayNode()
        visit(ctx.lhs)
        val _contextual = mutableMapOf<String, ArrayNode>()
        contextual.keys.forEach { key ->
            _contextual[key] = mapper.createArrayNode()
        }
        val _positional = mutableMapOf<String, ArrayNode>()
        positional.keys.forEach { key ->
            _positional[key] = mapper.createArrayNode()
        }
        val lhs = pop()
        lhs.forEachIndexed { index, lhe -> // left hand element
            context.push(lhe)
            loop.push(index)
            push(lhe)
            visit(ctx.rhs)
            val rhs = pop()
            rhs.forEach { rhe -> // right hand element
                when (rhe) {
                    is NumericNode -> {
                        val value = rhe.asInt()
                        val offset = if (value < 0) lhs.size() + value else value
                        if (index == offset) {
                            exp.add(lhe)
                        }
                    }

                    is RangeNode -> if (rhe.indexes.map {
                            val value = it.asInt()
                            val offset = if (value < 0) lhs.size() + value else value
                            offset
                        }.contains(index)) {
                        exp.add(lhe)
                    }

                    else -> {
                        if (functions.boolean(rhe).asBoolean()) {
                            contextual.forEach { (key, ref) ->
                                _contextual[key]?.add(ref[index])
                            }
                            positional.forEach { (key, ref) ->
                                _positional[key]?.add(ref[index])
                            }
                            exp.add(lhe)
                        }
                    }
                }
            }
            loop.pop()
            context.pop()
        }
        this.contextual.putAll(_contextual)
        this.positional.putAll(_positional)
        return push(exp)
    }

    override fun visitGt(ctx: JSongParser.GtContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.gt(lhs, rhs))
    }

    override fun visitGte(ctx: JSongParser.GteContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.gte(lhs, rhs))
    }

    override fun visitIn(ctx: JSongParser.InContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.include(lhs, rhs))
    }

    override fun visitJsong(ctx: JSongParser.JsongContext): JsonNode? {
        ctx.exp()?.let { visit(it) }
        return stack.firstOrNull()?.let { if (isToFlatten) functions.flatten(it) else it }
    }

    override fun visitLt(ctx: JSongParser.LtContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.lt(lhs, rhs))
    }

    override fun visitLte(ctx: JSongParser.LteContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.lte(lhs, rhs))
    }

    override fun visitMap(ctx: JSongParser.MapContext): JsonNode? {
        val exp = mapper.createArrayNode()
        visit(ctx.lhs)
        val lhs = pop()
        lhs.forEachIndexed { index, lhe ->
            context.push(lhe)
            loop.push(index)
            when (lhe) {
                is RangeNode -> lhe.indexes.forEach { lhi ->
                    loop.push(lhi.asInt())
                    push(lhi)
                    visit(ctx.rhs)
                    exp.addAll(pop())
                    loop.pop()
                }

                else -> {
                    push(lhe)
                    visit(ctx.rhs)
                    exp.addAll(pop())
                }
            }
            loop.pop()
            context.pop()
        }
        return push(exp)
    }

    override fun visitMapContextBinding(ctx: JSongParser.MapContextBindingContext): JsonNode? {
        val bnd = mapper.createArrayNode()
        val exp = mapper.createArrayNode()
        visit(ctx.lhs)
        val lhs = pop()
        lhs.forEach { lhe ->
            push(lhe)
            visit(ctx.rhs)
            val rhs = pop()
            rhs.forEach { rhe ->
                bnd.add(rhe)
                exp.add(lhe)
            }
        }
        contextual.forEach { (label, ref) ->
            contextual[label] = stretch(ref, bnd.size())
        }
        contextual[ctx.label().text] = bnd
        return push(exp)
    }

    override fun visitMapPositionBinding(ctx: JSongParser.MapPositionBindingContext): JsonNode? {
        val bnd = mapper.createArrayNode()
        visit(ctx.lhs)
        val lhs = pop()
        lhs.forEach { lhe ->
            push(lhe)
            visit(ctx.rhs)
            val rhs = pop()
            rhs.forEach { rhe ->
                bnd.add(rhe)
            }
        }
        positional.forEach { (label, ref) ->
            positional[label] = stretch(ref, bnd.size())
        }
        positional[ctx.label().text] = bnd
        return push(bnd)
    }

    override fun visitMul(ctx: JSongParser.MulContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.mul(lhs, rhs))
    }

    override fun visitOr(ctx: JSongParser.OrContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.or(lhs, rhs))
    }

    override fun visitNe(ctx: JSongParser.NeContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.ne(lhs, rhs))
    }

    override fun visitNihil(ctx: JSongParser.NihilContext): JsonNode? {
        return push(NullNode.instance)
    }

    override fun visitNumber(ctx: JSongParser.NumberContext): JsonNode? {
        return push(DecimalNode(ctx.NUMBER().text.toBigDecimal()))
    }

    override fun visitNumericAggregateFunction(ctx: JSongParser.NumericAggregateFunctionContext): JsonNode? {
        val args = mutableListOf<JsonNode>()
        ctx.exp().forEach { arg ->
            visit(arg)
            pop().let { args.add(it) }
        }
        val fnc = ctx.num_aggregate_fun()
        val exp = when {
            fnc.AVERAGE() != null -> when (args.size) {
                0 -> functions.average(pop())
                1 -> functions.average(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.AVERAGE}")
            }

            fnc.MAX() != null -> when (args.size) {
                0 -> functions.max(pop())
                1 -> functions.max(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MAX}")
            }

            fnc.MIN() != null -> when (args.size) {
                0 -> functions.min(pop())
                1 -> functions.min(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MIN}")
            }

            fnc.SUM() != null -> when (args.size) {
                0 -> functions.sum(pop())
                1 -> functions.sum(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SUM}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)
    }

    override fun visitNumericFunction(ctx: JSongParser.NumericFunctionContext): JsonNode? {
        val args = mutableListOf<JsonNode>()
        ctx.exp().forEach { arg ->
            visit(arg)
            pop().let { args.add(it) }
        }
        val fnc = ctx.num_fun()
        val exp = when {
            fnc.ABS() != null -> when (args.size) {
                0 -> functions.abs(pop())
                1 -> functions.abs(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ABS}")
            }

            fnc.CEIL() != null -> when (args.size) {
                0 -> functions.ceil(pop())
                1 -> functions.ceil(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.CEIL}")
            }

            fnc.FLOOR() != null -> when (args.size) {
                0 -> functions.floor(pop())
                1 -> functions.floor(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FLOOR}")
            }

            fnc.FORMAT_BASE() != null -> when (args.size) {
                0 -> functions.formatBase(pop())
                1 -> functions.formatBase(args[0])
                2 -> functions.formatBase(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FORMAT_BASE}")
            }

            fnc.FORMAT_INTEGER() != null -> when (args.size) {
                1 -> functions.formatInteger(pop(), args[0])
                2 -> functions.formatInteger(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FORMAT_INTEGER}")
            }

            fnc.FORMAT_NUMBER() != null -> when (args.size) {
                1 -> functions.formatNumber(pop(), args[0])
                2 -> functions.formatNumber(args[0], args[1])
                3 -> functions.formatNumber(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FORMAT_NUMBER}")
            }

            fnc.NUMBER_OF() != null -> when (args.size) {
                0 -> functions.number(pop())
                1 -> functions.number(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.NUMBER}")
            }

            fnc.PARSE_INTEGER() != null -> when (args.size) {
                1 -> functions.parseInteger(pop(), args[0])
                2 -> functions.parseInteger(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.PARSE_INTEGER}")
            }

            fnc.POWER() != null -> when (args.size) {
                1 -> functions.power(pop(), args[0])
                2 -> functions.power(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.POWER}")
            }

            fnc.RANDOM() != null -> when (args.size) {
                0 -> functions.randomFrom(random)
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.RANDOM}")
            }

            fnc.ROUND() != null -> when (args.size) {
                0 -> functions.round(pop())
                1 -> functions.round(args[0])
                2 -> functions.round(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ROUND}")
            }

            fnc.SQRT() != null -> when (args.size) {
                0 -> functions.sqrt(pop())
                1 -> functions.sqrt(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SQRT}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)
    }

    override fun visitObj(ctx: JSongParser.ObjContext): JsonNode? {
        val exp = mapper.createObjectNode()
        ctx.pair().forEachIndexed { index, pairCtx ->
            visit(pairCtx.lhs)
            val lhs = functions.flatten(pop())?.asText() ?: index.toString()
            visit(pairCtx.rhs)
            val rhs = functions.flatten(pop()) ?: NullNode.instance
            exp.set<JsonNode>(lhs, if (isToFlatten) functions.flatten(rhs) else rhs)
        }
        return push(exp)
    }

    override fun visitObjectFunction(ctx: JSongParser.ObjectFunctionContext): JsonNode? {
        val args = mutableListOf<JsonNode>()
        ctx.exp().forEach { arg ->
            visit(arg)
            functions.flatten(pop())?.let { args.add(it) }
        }
        val fnc = ctx.obj_fun()
        val exp = when {
            fnc.ASSERT() != null -> when (args.size) {
                1 -> functions.assert(pop(), args[0])
                2 -> functions.assert(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ASSERT}")
            }

            fnc.ERROR() != null -> when (args.size) {
                0 -> functions.error(pop())
                1 -> functions.error(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ERROR}")
            }

            fnc.KEYS() != null -> when (args.size) {
                0 -> functions.keys(pop())
                1 -> functions.keys(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.KEYS}")
            }

            fnc.LOOKUP() != null -> when (args.size) {
                1 -> functions.lookup(pop(), args[0])
                2 -> functions.lookup(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.LOOKUP}")
            }

            fnc.MERGE() != null -> when (args.size) {
                0 -> functions.merge(pop())
                1 -> functions.merge(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MERGE}")
            }

            fnc.SPREAD() != null -> when (args.size) {
                0 -> functions.spread(pop())
                1 -> functions.spread(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SPREAD}")
            }

            fnc.TYPE() != null -> when (args.size) {
                0 -> functions.type(pop())
                1 -> functions.type(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.TYPE}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)

    }

    override fun visitPath(ctx: JSongParser.PathContext): JsonNode? {
        scope.firstOrNull()?.let { push(it) }
        return push(select(pop(), PathNode(ctx.text)))
    }

    override fun visitRange(ctx: JSongParser.RangeContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.min)
        val min = functions.flatten(stack.pop())?.decimalValue() ?: BigDecimal.ZERO
        push(context)
        visit(ctx.max)
        val max = functions.flatten(stack.pop())?.decimalValue() ?: BigDecimal.ZERO
        return push(RangeNode.of(min, max, mapper.nodeFactory))
    }

    override fun visitRanges(ctx: JSongParser.RangesContext): JsonNode? {
        val exp = RangesNode(mapper.nodeFactory)
        ctx.range().forEach {
            visit(it)
            exp.add(pop())
        }
        return push(exp)
    }

    override fun visitRegex(ctx: JSongParser.RegexContext): JsonNode? {
        return push(RegexNode(ctx.REGEX().text))
    }

    override fun visitReminder(ctx: JSongParser.ReminderContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.reminder(lhs, rhs))
    }

    override fun visitRoot(ctx: JSongParser.RootContext?): JsonNode? {
        return push(root)
    }

    override fun visitScope(ctx: JSongParser.ScopeContext): JsonNode? {
        val context = pop()
        ctx.exp().forEach {
            push(context)
            visit(it)
        }
        return stack.firstOrNull()
    }

    override fun visitSub(ctx: JSongParser.SubContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.sub(lhs, rhs))
    }

    override fun visitText(ctx: JSongParser.TextContext): JsonNode? {
        return push(TextNode(ctx.text.substring(1, ctx.text.length - 1)))
    }

    override fun visitTextFunction(ctx: JSongParser.TextFunctionContext): JsonNode? {
        val args = mutableListOf<JsonNode>()
        ctx.exp().forEach { arg ->
            visit(arg)
            pop().let { args.add(it) }
        }
        val fnc = ctx.text_fun()
        val exp = when {
            fnc.BASE64_DECODE() != null -> when (args.size) {
                0 -> functions.base64decode(pop())
                1 -> functions.base64decode(args[0])
                else -> throw IllegalArgumentException("\${ctx.text} requires ${Syntax.BASE64_DECODE}")
            }

            fnc.BASE64_ENCODE() != null -> when (args.size) {
                0 -> functions.base64encode(pop())
                1 -> functions.base64encode(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.BASE64_ENCODE}")
            }

            fnc.CONTAINS() != null -> when (args.size) {
                1 -> functions.contains(pop(), args[0])
                2 -> functions.contains(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.CONTAINS}")
            }

            fnc.DECODE_URL() != null -> when (args.size) {
                0 -> functions.decodeUrl(pop())
                1 -> functions.decodeUrl(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.DECODE_URL}")
            }

            fnc.DECODE_URL_COMPONENT() != null -> when (args.size) {
                0 -> functions.decodeUrlComponent(pop())
                1 -> functions.decodeUrlComponent(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.DECODE_URL_COMPONENT}")
            }

            fnc.ENCODE_URL() != null -> when (args.size) {
                0 -> functions.encodeUrl(pop())
                1 -> functions.encodeUrl(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ENCODE_URL}")
            }

            fnc.ENCODE_URL_COMPONENT() != null -> when (args.size) {
                0 -> functions.encodeUrlComponent(pop())
                1 -> functions.encodeUrlComponent(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ENCODE_URL_COMPONENT}")
            }

            fnc.EVAL() != null -> when (args.size) {
                1 -> functions.eval(args[0], pop())
                2 -> functions.eval(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.EVAL}")
            }

            fnc.JOIN() != null -> when (args.size) {
                0 -> functions.join(pop())
                1 -> functions.join(args[0])
                2 -> functions.join(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.JOIN}")
            }

            fnc.LENGTH() != null -> when (args.size) {
                0 -> functions.length(pop())
                1 -> functions.length(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.LENGTH_OF}")
            }

            fnc.LOWERCASE() != null -> when (args.size) {
                0 -> functions.lowercase(pop())
                1 -> functions.lowercase(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.LOWERCASE}")
            }

            fnc.MATCH() != null -> when (args.size) {
                1 -> functions.match(pop(), args[0])
                2 -> functions.match(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MATCH}")
            }

            fnc.PAD() != null -> when (args.size) {
                1 -> functions.pad(pop(), args[0])
                2 -> functions.pad(args[0], args[1])
                3 -> functions.pad(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.PAD}")
            }

            fnc.REPLACE() != null -> when (args.size) {
                2 -> functions.replace(pop(), args[0], args[1])
                3 -> functions.replace(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.REPLACE}")
            }

            fnc.SPLIT() != null -> when (args.size) {
                1 -> functions.split(pop(), args[0])
                2 -> functions.split(args[0], args[1])
                3 -> functions.split(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SPLIT}")
            }

            fnc.STRING_OF() != null -> when (args.size) {
                0 -> functions.string(pop())
                1 -> functions.string(args[0])
                2 -> functions.string(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.STRING_OF}")
            }

            fnc.SUBSTRING() != null -> when (args.size) {
                0 -> functions.substring(pop())
                1 -> functions.substring(args[0])
                2 -> functions.substring(args[0], args[1])
                3 -> functions.substring(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SUBSTRING}")
            }

            fnc.SUBSTRING_AFTER() != null -> when (args.size) {
                1 -> functions.substringAfter(pop(), args[0])
                2 -> functions.substringAfter(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SUBSTRING_AFTER}")
            }

            fnc.SUBSTRING_BEFORE() != null -> when (args.size) {
                1 -> functions.substringBefore(pop(), args[0])
                2 -> functions.substringBefore(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SUBSTRING_BEFORE}")
            }

            fnc.TRIM() != null -> when (args.size) {
                0 -> functions.trim(pop())
                1 -> functions.trim(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.TRIM}")
            }

            fnc.UPPERCASE() != null -> when (args.size) {
                0 -> functions.uppercase(pop())
                1 -> functions.uppercase(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.UPPERCASE}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)
    }

    override fun visitTimeFunction(ctx: JSongParser.TimeFunctionContext): JsonNode? {
        val args = mutableListOf<JsonNode>()
        ctx.exp().forEach { arg ->
            visit(arg)
            pop().let { args.add(it) }
        }
        val fnc = ctx.time_fun()
        val exp = when {
            fnc.FROM_MILLIS() != null -> TextNode(
                when (args.size) {
                    0 -> functions.fromMillis(pop())
                    1 -> functions.fromMillis(args[0])
                    2 -> functions.fromMillis(args[0], args[1])
                    3 -> functions.fromMillis(args[0], args[1], args[2])
                    else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FROM_MILLIS}")
                }
            )

            fnc.MILLIS() != null -> when (args.size) {
                0 -> functions.millis(time)
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MILLIS}")
            }

            fnc.NOW() != null -> when (args.size) {
                0 -> functions.now(time)
                1 -> functions.now(time, args[0])
                2 -> functions.now(time, args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.NOW}")
            }

            fnc.TO_MILLIS() != null -> when (args.size) {
                0 -> functions.toMillis(pop())
                1 -> functions.toMillis(args[0])
                2 -> functions.toMillis(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.TO_MILLIS}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)
    }

    override fun visitVar(ctx: JSongParser.VarContext): JsonNode? {
        val label = ctx.label().text
        val value: JsonNode? = if (positional.contains(label)) {
            val index = positional[label]?.indexOf(context.peek()) ?: -1
            if (index > -1) IntNode(index + 1) else null
        } else if (contextual.contains(label)) {
            when (loop.isEmpty()) {
                true -> contextual[label]
                else -> contextual[label]?.get(loop.peek())
            }
        } else if (variables.contains(label)) {
            variables[label]?.value
        } else null
        return push(value ?: mapper.createArrayNode())
    }

    override fun visitVarBinding(ctx: JSongParser.VarBindingContext): JsonNode? {
        val exp = mapper.createArrayNode()
        visit(ctx.exp())
        exp.addAll(pop())
        val name = ctx.label().text
        variables[name] = VarNode(name, exp, mapper.nodeFactory)
        return push(exp)
    }

    override fun visitWildcardPostfix(ctx: JSongParser.WildcardPostfixContext): JsonNode? {
        val exp = mapper.createArrayNode()
        visit(ctx.exp())
        pop().forEach { element ->
            element.fields().forEach { property ->
                exp.add(property.value)
            }
        }
        return push(exp)
    }

    override fun visitWildcardPrefix(ctx: JSongParser.WildcardPrefixContext): JsonNode? {
        val exp = mapper.createArrayNode()
        pop().forEach { element ->
            element.fields().forEach { property ->
                push(property.value)
                visit(ctx.exp())
                functions.flatten(pop())?.let { exp.add(it) }
            }
        }
        return push(exp)
    }

} //~ Processor
