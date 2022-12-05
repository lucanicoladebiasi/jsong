package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.antlr.JSongBaseVisitor
import org.jsong.antlr.JSongLexer
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

    private val library = Functions(mapper, random, time)

    private val functions = mutableMapOf<String, FunNode>()

    @Volatile
    private var isToFlatten = true

    private val loop = ArrayDeque<Int>()

    private val scope = ArrayDeque<JsonNode>() // todo: apply to maps?

    private val stack = ArrayDeque<ArrayNode>()

    internal val variables = mutableMapOf<String, VarNode>()

    init {
        push(root)
    }

    private fun descendants(node: JsonNode?): ArrayNode {
        val exp = mapper.createArrayNode()
        library.array(node).forEach { element ->
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
        stack.push(library.array(library.flatten(node)))
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

    override fun visitAdd(ctx: JSongParser.AddContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(library.add(lhs, rhs))
    }

    override fun visitAnd(ctx: JSongParser.AndContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(library.and(library.flatten(lhs), library.flatten(rhs)))
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
                1 -> library.append(pop(), args[0])
                2 -> library.append(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.APPEND}")
            }

            fnc.COUNT() != null -> when (args.size) {
                0 -> library.count(pop())
                1 -> library.count(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.COUNT}")
            }

            fnc.DISTINCT() != null -> mapper.createArrayNode().addAll(
                when (args.size) {
                    0 -> library.distinct(pop())
                    1 -> library.distinct(args[0])
                    else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.DISTINCT}")
                }
            )

            fnc.REVERSE() != null -> when (args.size) {
                0 -> library.reverse(pop())
                1 -> library.reverse(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.REVERSE}")
            }

            fnc.SHUFFLE() != null -> when (args.size) {
                0 -> library.shuffle(pop(), random)
                1 -> library.shuffle(args[0], random)
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SHUFFLE}")
            }

            fnc.SORT() != null -> when (args.size) {
                0 -> library.sort(pop())
                1 -> library.sort(args[0])
                2 -> library.sort(args[0])
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
                0 -> library.boolean(pop())
                1 -> library.boolean(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.BOOLEAN}")
            }

            fnc.EXISTS() != null -> when (args.size) {
                0 -> library.exists(pop())
                1 -> library.exists(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.EXISTS}")
            }

            fnc.NOT() != null -> when (args.size) {
                0 -> library.not(pop())
                1 -> library.not(args[0])
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
        return push(library.concatenate(library.flatten(lhs), library.flatten(rhs)))
    }

    override fun visitContext(ctx: JSongParser.ContextContext): JsonNode? {
        return push(stack.firstOrNull())
    }

    override fun visitDefineFunction(ctx: JSongParser.DefineFunctionContext): JsonNode? {
        val name = ctx.label()[0].text
        val args = mutableListOf<VarNode>()
        for (i in 1 until ctx.label().size) {
            args.add(VarNode(ctx.label()[i].text))
        }
        val body = ctx.exp().text
        val exp = FunNode(name, args, body)
        functions.put(name, exp)
        return push(exp)
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
        return push(library.div(lhs, rhs))
    }

    override fun visitEq(ctx: JSongParser.EqContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        val res = library.eq(lhs, rhs)
        return push(res)
    }

    override fun visitFilter(ctx: JSongParser.FilterContext): JsonNode? {
        val exp = mapper.createArrayNode()
        visit(ctx.lhs)
        val _contextual = mutableMapOf<String, VarNode>()
        val _positional = mutableMapOf<String, VarNode>()
        variables.filter { it.value is ContextualVarNode }.keys.forEach { key ->
            _contextual[key] = ContextualVarNode(key)
        }
        variables.filter { it.value is PositionalVarNode }.keys.forEach { key ->
            _positional[key] = PositionalVarNode(key)
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
                        if (library.boolean(rhe).asBoolean()) {
                            variables.filter {
                                it.value is ContextualVarNode
                            }.forEach { (key, ref) ->
                                _contextual[key]?.value?.add(ref.value[index])
                            }
                            variables.filter {
                                it.value is PositionalVarNode
                            }.forEach { (key, ref) ->
                                _positional[key]?.value?.add(ref.value[index])
                            }
                            exp.add(lhe)
                        }
                    }
                }
            }
            loop.pop()
            context.pop()
        }
        variables.putAll(_contextual)
        variables.putAll(_positional)
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
        return push(library.gt(lhs, rhs))
    }

    override fun visitGte(ctx: JSongParser.GteContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(library.gte(lhs, rhs))
    }

    override fun visitIn(ctx: JSongParser.InContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(library.include(lhs, rhs))
    }

    override fun visitJsong(ctx: JSongParser.JsongContext): JsonNode? {
        ctx.exp()?.let { visit(it) }
        return stack.firstOrNull()?.let { if (isToFlatten) library.flatten(it) else it }
    }

    override fun visitLambdaFunction(ctx: JSongParser.LambdaFunctionContext): JsonNode? {
        val labs = ctx.label().map { it.text }
        val exps = ctx.exp()
        val args = mutableMapOf<String, VarNode>()
        // exp.size = labs.size + 1
        val pop = pop()
        for (i in labs.indices) {
            push(pop)
            visit(exps[i + 1])
            args.put(labs[i], VarNode(labs[i], pop()))
        }

        variables.putAll(args)
        push(pop)
        visit(exps[0])
        val exp = pop()
        return push(exp)
    }

    override fun visitLt(ctx: JSongParser.LtContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(library.lt(lhs, rhs))
    }

    override fun visitLte(ctx: JSongParser.LteContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(library.lte(lhs, rhs))
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
        val exp = mapper.createArrayNode()
        val value = mapper.createArrayNode()
        visit(ctx.lhs)
        pop().forEach { lhe ->
            push(lhe)
            visit(ctx.rhs)
            pop().forEach { rhe ->
                value.add(rhe)
                exp.add(lhe)
            }
        }
        variables.filter { it.value is ContextualVarNode }.forEach {
            it.value.stretch(value.size())
        }
        val name = ctx.label().text
        variables[name] = ContextualVarNode(name, value, mapper.nodeFactory)
        return push(exp)
    }

    override fun visitMapPositionBinding(ctx: JSongParser.MapPositionBindingContext): JsonNode? {
        val value = mapper.createArrayNode()
        visit(ctx.lhs)
        pop().forEach { lhe ->
            push(lhe)
            visit(ctx.rhs)
            pop().forEach { rhe ->
                value.add(rhe)
            }
        }
        variables.filter { it.value is PositionalVarNode }.forEach {
            it.value.stretch(value.size())
        }
        val name = ctx.label().text
        variables[name] = PositionalVarNode(name, value, mapper.nodeFactory)
        return push(value)
    }

    override fun visitMul(ctx: JSongParser.MulContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(library.mul(lhs, rhs))
    }

    override fun visitOr(ctx: JSongParser.OrContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(library.or(lhs, rhs))
    }

    override fun visitNe(ctx: JSongParser.NeContext): JsonNode? {
        val context = pop()
        push(context)
        visit(ctx.lhs)
        val lhs = pop()
        push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(library.ne(lhs, rhs))
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
                0 -> library.average(pop())
                1 -> library.average(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.AVERAGE}")
            }

            fnc.MAX() != null -> when (args.size) {
                0 -> library.max(pop())
                1 -> library.max(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MAX}")
            }

            fnc.MIN() != null -> when (args.size) {
                0 -> library.min(pop())
                1 -> library.min(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MIN}")
            }

            fnc.SUM() != null -> when (args.size) {
                0 -> library.sum(pop())
                1 -> library.sum(args[0])
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
                0 -> library.abs(pop())
                1 -> library.abs(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ABS}")
            }

            fnc.CEIL() != null -> when (args.size) {
                0 -> library.ceil(pop())
                1 -> library.ceil(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.CEIL}")
            }

            fnc.FLOOR() != null -> when (args.size) {
                0 -> library.floor(pop())
                1 -> library.floor(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FLOOR}")
            }

            fnc.FORMAT_BASE() != null -> when (args.size) {
                0 -> library.formatBase(pop())
                1 -> library.formatBase(args[0])
                2 -> library.formatBase(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FORMAT_BASE}")
            }

            fnc.FORMAT_INTEGER() != null -> when (args.size) {
                1 -> library.formatInteger(pop(), args[0])
                2 -> library.formatInteger(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FORMAT_INTEGER}")
            }

            fnc.FORMAT_NUMBER() != null -> when (args.size) {
                1 -> library.formatNumber(pop(), args[0])
                2 -> library.formatNumber(args[0], args[1])
                3 -> library.formatNumber(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FORMAT_NUMBER}")
            }

            fnc.NUMBER_OF() != null -> when (args.size) {
                0 -> library.number(pop())
                1 -> library.number(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.NUMBER}")
            }

            fnc.PARSE_INTEGER() != null -> when (args.size) {
                1 -> library.parseInteger(pop(), args[0])
                2 -> library.parseInteger(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.PARSE_INTEGER}")
            }

            fnc.POWER() != null -> when (args.size) {
                1 -> library.power(pop(), args[0])
                2 -> library.power(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.POWER}")
            }

            fnc.RANDOM() != null -> when (args.size) {
                0 -> library.randomFrom(random)
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.RANDOM}")
            }

            fnc.ROUND() != null -> when (args.size) {
                0 -> library.round(pop())
                1 -> library.round(args[0])
                2 -> library.round(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ROUND}")
            }

            fnc.SQRT() != null -> when (args.size) {
                0 -> library.sqrt(pop())
                1 -> library.sqrt(args[0])
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
            val lhs = library.flatten(pop())?.asText() ?: index.toString()
            visit(pairCtx.rhs)
            val rhs = library.flatten(pop()) ?: NullNode.instance
            exp.set<JsonNode>(lhs, if (isToFlatten) library.flatten(rhs) else rhs)
        }
        return push(exp)
    }

    override fun visitObjectFunction(ctx: JSongParser.ObjectFunctionContext): JsonNode? {
        val args = mutableListOf<JsonNode>()
        ctx.exp().forEach { arg ->
            visit(arg)
            library.flatten(pop())?.let { args.add(it) }
        }
        val fnc = ctx.obj_fun()
        val exp = when {
            fnc.ASSERT() != null -> when (args.size) {
                1 -> library.assert(pop(), args[0])
                2 -> library.assert(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ASSERT}")
            }

            fnc.ERROR() != null -> when (args.size) {
                0 -> library.error(pop())
                1 -> library.error(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ERROR}")
            }

            fnc.KEYS() != null -> when (args.size) {
                0 -> library.keys(pop())
                1 -> library.keys(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.KEYS}")
            }

            fnc.LOOKUP() != null -> when (args.size) {
                1 -> library.lookup(pop(), args[0])
                2 -> library.lookup(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.LOOKUP}")
            }

            fnc.MERGE() != null -> when (args.size) {
                0 -> library.merge(pop())
                1 -> library.merge(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MERGE}")
            }

            fnc.SPREAD() != null -> when (args.size) {
                0 -> library.spread(pop())
                1 -> library.spread(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SPREAD}")
            }

            fnc.TYPE() != null -> when (args.size) {
                0 -> library.type(pop())
                1 -> library.type(args[0])
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
        val min = library.flatten(stack.pop())?.decimalValue() ?: BigDecimal.ZERO
        push(context)
        visit(ctx.max)
        val max = library.flatten(stack.pop())?.decimalValue() ?: BigDecimal.ZERO
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

    override fun visitRecallFunction(ctx: JSongParser.RecallFunctionContext): JsonNode? {
        val name = ctx.label().text
        val function = functions[name]
        if (function != null) {
            val pro = Processor(mapper, random, time, null)
            val pop = pop()
            for(i in function.args.indices) {
                push(pop)
                visit(ctx.exp(i))
                pro.variables.put(function.args[i].name.textValue(), VarNode(function.args[i].name.textValue(), pop()))
            }
            val parser = JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(function.body))))
            val result = pro.visit(parser.jsong())
            return push(result)
        }
        return stack.peek()
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
        return push(library.reminder(lhs, rhs))
    }

    override fun visitRoot(ctx: JSongParser.RootContext?): JsonNode? {
        return push(root)
    }

    override fun visitScope(ctx: JSongParser.ScopeContext): JsonNode? {
        val context = pop()
        ctx.exp().forEach {
            println("<${it.text}>")
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
        return push(library.sub(lhs, rhs))
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
                0 -> library.base64decode(pop())
                1 -> library.base64decode(args[0])
                else -> throw IllegalArgumentException("\${ctx.text} requires ${Syntax.BASE64_DECODE}")
            }

            fnc.BASE64_ENCODE() != null -> when (args.size) {
                0 -> library.base64encode(pop())
                1 -> library.base64encode(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.BASE64_ENCODE}")
            }

            fnc.CONTAINS() != null -> when (args.size) {
                1 -> library.contains(pop(), args[0])
                2 -> library.contains(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.CONTAINS}")
            }

            fnc.DECODE_URL() != null -> when (args.size) {
                0 -> library.decodeUrl(pop())
                1 -> library.decodeUrl(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.DECODE_URL}")
            }

            fnc.DECODE_URL_COMPONENT() != null -> when (args.size) {
                0 -> library.decodeUrlComponent(pop())
                1 -> library.decodeUrlComponent(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.DECODE_URL_COMPONENT}")
            }

            fnc.ENCODE_URL() != null -> when (args.size) {
                0 -> library.encodeUrl(pop())
                1 -> library.encodeUrl(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ENCODE_URL}")
            }

            fnc.ENCODE_URL_COMPONENT() != null -> when (args.size) {
                0 -> library.encodeUrlComponent(pop())
                1 -> library.encodeUrlComponent(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.ENCODE_URL_COMPONENT}")
            }

            fnc.EVAL() != null -> when (args.size) {
                1 -> library.eval(args[0], pop())
                2 -> library.eval(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.EVAL}")
            }

            fnc.JOIN() != null -> when (args.size) {
                0 -> library.join(pop())
                1 -> library.join(args[0])
                2 -> library.join(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.JOIN}")
            }

            fnc.LENGTH() != null -> when (args.size) {
                0 -> library.length(pop())
                1 -> library.length(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.LENGTH_OF}")
            }

            fnc.LOWERCASE() != null -> when (args.size) {
                0 -> library.lowercase(pop())
                1 -> library.lowercase(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.LOWERCASE}")
            }

            fnc.MATCH() != null -> when (args.size) {
                1 -> library.match(pop(), args[0])
                2 -> library.match(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MATCH}")
            }

            fnc.PAD() != null -> when (args.size) {
                1 -> library.pad(pop(), args[0])
                2 -> library.pad(args[0], args[1])
                3 -> library.pad(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.PAD}")
            }

            fnc.REPLACE() != null -> when (args.size) {
                2 -> library.replace(pop(), args[0], args[1])
                3 -> library.replace(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.REPLACE}")
            }

            fnc.SPLIT() != null -> when (args.size) {
                1 -> library.split(pop(), args[0])
                2 -> library.split(args[0], args[1])
                3 -> library.split(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SPLIT}")
            }

            fnc.STRING_OF() != null -> when (args.size) {
                0 -> library.string(pop())
                1 -> library.string(args[0])
                2 -> library.string(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.STRING_OF}")
            }

            fnc.SUBSTRING() != null -> when (args.size) {
                0 -> library.substring(pop())
                1 -> library.substring(args[0])
                2 -> library.substring(args[0], args[1])
                3 -> library.substring(args[0], args[1], args[2])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SUBSTRING}")
            }

            fnc.SUBSTRING_AFTER() != null -> when (args.size) {
                1 -> library.substringAfter(pop(), args[0])
                2 -> library.substringAfter(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SUBSTRING_AFTER}")
            }

            fnc.SUBSTRING_BEFORE() != null -> when (args.size) {
                1 -> library.substringBefore(pop(), args[0])
                2 -> library.substringBefore(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SUBSTRING_BEFORE}")
            }

            fnc.TRIM() != null -> when (args.size) {
                0 -> library.trim(pop())
                1 -> library.trim(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.TRIM}")
            }

            fnc.UPPERCASE() != null -> when (args.size) {
                0 -> library.uppercase(pop())
                1 -> library.uppercase(args[0])
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
                    0 -> library.fromMillis(pop())
                    1 -> library.fromMillis(args[0])
                    2 -> library.fromMillis(args[0], args[1])
                    3 -> library.fromMillis(args[0], args[1], args[2])
                    else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.FROM_MILLIS}")
                }
            )

            fnc.MILLIS() != null -> when (args.size) {
                0 -> library.millis(time)
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MILLIS}")
            }

            fnc.NOW() != null -> when (args.size) {
                0 -> library.now(time)
                1 -> library.now(time, args[0])
                2 -> library.now(time, args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.NOW}")
            }

            fnc.TO_MILLIS() != null -> when (args.size) {
                0 -> library.toMillis(pop())
                1 -> library.toMillis(args[0])
                2 -> library.toMillis(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.TO_MILLIS}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)
    }

    override fun visitVar(ctx: JSongParser.VarContext): JsonNode? {
        val exp = variables[ctx.label().text]?.let {
            when (it) {
                is ContextualVarNode -> {
                    when (loop.isEmpty()) {
                        true -> it.value
                        else -> it.value[loop.peek()]
                    }
                }

                is PositionalVarNode -> {
                    val index = it.value.indexOf(context.peek())
                    if (index > -1) IntNode(index + 1) else null
                }

                else -> {
                    it.value
                }
            }
        }
        return push(exp ?: mapper.createArrayNode())
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
                library.flatten(pop())?.let { exp.add(it) }
            }
        }
        return push(exp)
    }

} //~ Processor
