package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.jsong.antlr.JSongBaseVisitor
import org.jsong.antlr.JSongParser
import java.util.*
import kotlin.random.Random

class Processor internal constructor(
    private val mapper: ObjectMapper,
    private val random: Random,
    private val root: JsonNode?
) : JSongBaseVisitor<JsonNode?>() {

    private val functions = Functions(mapper)
    private val stack = ArrayDeque<JsonNode>()
    private val variables = ArrayDeque<MutableMap<String, JsonNode?>>()

    @Volatile
    private var isToFlatten = true

    init {
        variables.push(mutableMapOf())
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
        if (node != null) stack.push(node)
        return node
    }

    private fun pop(): JsonNode? {
        return when (stack.isEmpty()) {
            true -> null
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
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.add(lhs, rhs))
    }

    override fun visitAnd(ctx: JSongParser.AndContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.and(lhs, rhs))
    }

    override fun visitArray(ctx: JSongParser.ArrayContext): JsonNode? {
        val exp = mapper.createArrayNode()
        ctx.children.forEach {
            visit(it)
            pop()?.let { exp.add(it) }
        }
        return push(exp)
    }

    override fun visitArrayFunction(ctx: JSongParser.ArrayFunctionContext): JsonNode? {
        val args = mutableListOf<JsonNode>()
        val context = pop()
        ctx.exp().forEach { arg ->
            if (context != null) push(context)
            visit(arg)
            pop()?.let { args.add(it) }
        }
        val fnc = ctx.array_fun()
        val exp = when {
            fnc.APPEND() != null -> when (args.size) {
                1 -> functions.append(context, args[0])
                2 -> functions.append(args[0], args[1])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.APPEND}")
            }

            fnc.COUNT() != null -> when (args.size) {
                0 -> functions.count(context)
                1 -> functions.count(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.COUNT}")
            }

            fnc.DISTINCT() != null -> mapper.createArrayNode().addAll(
                when (args.size) {
                    0 -> functions.distinct(context)
                    1 -> functions.distinct(args[0])
                    else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.DISTINCT}")
                }
            )

            fnc.REVERSE() != null -> when (args.size) {
                0 -> functions.reverse(context)
                1 -> functions.reverse(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.REVERSE}")
            }

            fnc.SHUFFLE() != null -> when (args.size) {
                0 -> functions.shuffle(context, random)
                1 -> functions.shuffle(args[0], random)
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SHUFFLE}")
            }

            fnc.SORT() != null -> when (args.size) {
                0 -> functions.sort(context)
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
        return push(functions.array(pop()))
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
        val context = pop()
        ctx.exp().forEach { arg ->
            if (context != null) push(context)
            visit(arg)
            pop()?.let { args.add(it) }
        }
        val fnc = ctx.bool_fun()
        val exp = when {
            fnc.BOOLEAN() != null -> when (args.size) {
                0 -> functions.boolean(context)
                1 -> functions.boolean(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.BOOLEAN}")
            }

            fnc.EXISTS() != null -> when (args.size) {
                0 -> functions.exists(context)
                1 -> functions.exists(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.EXISTS}")
            }

            fnc.NOT() != null -> when (args.size) {
                0 -> functions.not(context)
                1 -> functions.not(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.NOT}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)
    }

    override fun visitConcatenate(ctx: JSongParser.ConcatenateContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.concatenate(lhs, rhs))
    }

    override fun visitContext(ctx: JSongParser.ContextContext): JsonNode? {
        return push(stack.firstOrNull())
    }

    override fun visitDescendants(ctx: JSongParser.DescendantsContext): JsonNode? {
        return push(descendants(pop()))
    }

    override fun visitDiv(ctx: JSongParser.DivContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.div(lhs, rhs))
    }

    override fun visitEq(ctx: JSongParser.EqContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.eq(lhs, rhs))
    }

    override fun visitFilter(ctx: JSongParser.FilterContext): JsonNode? {
        val exp = mapper.createArrayNode()
        visit(ctx.lhs)
        val lhs = functions.array(pop())
        visit(ctx.rhs)
        when (val rhs = pop()) {
            is NumericNode -> {
                val value = rhs.asInt()
                val offset = if (value < 0) lhs.size() + value else value
                lhs.get(offset)?.let { exp.add(it) }
            }

            is RangesNode -> {
                rhs.indexes.forEach { index ->
                    val value = index.asInt()
                    val offset = if (value < 0) lhs.size() + value else value
                    lhs.get(offset)?.let { exp.add(it) }
                }
            }

            else -> {
                lhs.forEach { element ->
                    push(element)
                    visit(ctx.rhs)
                    val predicate = pop()
                    if (functions.boolean(predicate).booleanValue()) {
                        exp.add(element)
                    }
                }
            }
        }
        return push(exp)
    }

    override fun visitGt(ctx: JSongParser.GtContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.gt(lhs, rhs))
    }

    override fun visitGte(ctx: JSongParser.GteContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.gte(lhs, rhs))
    }

    override fun visitIn(ctx: JSongParser.InContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.include(lhs, rhs))
    }

    override fun visitJsong(ctx: JSongParser.JsongContext): JsonNode? {
        ctx.exp()?.let { visit(it) }
        return stack.firstOrNull()?.let { if (isToFlatten) functions.flatten(it) else it }
    }

    override fun visitLt(ctx: JSongParser.LtContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.lt(lhs, rhs))
    }

    override fun visitLte(ctx: JSongParser.LteContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.lte(lhs, rhs))
    }

    override fun visitMap(ctx: JSongParser.MapContext): JsonNode? {
        val exp = mapper.createArrayNode()
        visit(ctx.lhs)
        when (val lhs = pop()) {
            is RangeNode -> lhs.indexes.forEach { index ->
                push(index)
                visit(ctx.rhs)
                pop()?.let { exp.addAll(functions.array(it)) }
            }

            is RangesNode -> lhs.indexes.forEach { index ->
                push(index)
                visit(ctx.rhs)
                pop()?.let { exp.addAll(functions.array(it)) }
            }

            else -> functions.array(lhs).forEach { element ->
                push(element)
                visit(ctx.rhs)
                pop()?.let { exp.addAll(functions.array(it)) }
            }
        }
        return push(exp)
    }

    override fun visitMul(ctx: JSongParser.MulContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.mul(lhs, rhs))
    }

    override fun visitOr(ctx: JSongParser.OrContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.or(lhs, rhs))
    }


    override fun visitNe(ctx: JSongParser.NeContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
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
        val context = pop()
        ctx.exp().forEach { arg ->
            if (context != null) push(context)
            visit(arg)
            pop()?.let { args.add(it) }
        }
        val fnc = ctx.num_aggregate()
        val exp = when {
            fnc.AVERAGE() != null -> when (args.size) {
                0 -> functions.average(context)
                1 -> functions.average(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.AVERAGE}")
            }

            fnc.MAX() != null -> when (args.size) {
                0 -> functions.max(context)
                1 -> functions.max(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MAX}")
            }

            fnc.MIN() != null -> when (args.size) {
                0 -> functions.min(context)
                1 -> functions.min(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.MIN}")
            }

            fnc.SUM() != null -> when (args.size) {
                0 -> functions.sum(context)
                1 -> functions.sum(args[0])
                else -> throw IllegalArgumentException("${ctx.text} requires ${Syntax.SUM}")
            }

            else -> throw UnsupportedOperationException("${ctx.text} not recognized")
        }
        return push(exp)
    }

    override fun visitObj(ctx: JSongParser.ObjContext): JsonNode? {
        val exp = mapper.createObjectNode()
        ctx.pair().forEachIndexed { index, pairCtx ->
            visit(pairCtx.lhs)
            val lhs = pop()?.asText() ?: index.toString()
            visit(pairCtx.rhs)
            val rhs = pop() ?: NullNode.instance
            exp.set<JsonNode>(lhs, rhs)
        }
        return push(exp)
    }

    override fun visitPath(ctx: JSongParser.PathContext): JsonNode? {
        return push(select(pop(), PathNode(ctx.text)))
    }

    override fun visitRange(ctx: JSongParser.RangeContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.min)
        val min = stack.pop()
        if (context != null) stack.push(context)
        visit(ctx.max)
        val max = stack.pop()
        return push(RangeNode.of(min.decimalValue(), max.decimalValue(), mapper.nodeFactory))
    }

    override fun visitRanges(ctx: JSongParser.RangesContext): JsonNode? {
        val exp = RangesNode(mapper.nodeFactory)
        ctx.range().forEach {
            visit(it)
            exp.add(pop())
        }
        return push(exp)
    }

    override fun visitReminder(ctx: JSongParser.ReminderContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.reminder(lhs, rhs))
    }

    override fun visitRoot(ctx: JSongParser.RootContext?): JsonNode? {
        return push(root)
    }

    override fun visitScope(ctx: JSongParser.ScopeContext): JsonNode? {
        variables.push(mutableMapOf())
        val context = pop()
        ctx.children.forEach {
            push(context)
            visit(it)
        }
        variables.pop()
        return stack.firstOrNull()
    }

    override fun visitSub(ctx: JSongParser.SubContext): JsonNode? {
        val context = stack.firstOrNull()
        visit(ctx.lhs)
        val lhs = pop()
        if (context != null) stack.push(context)
        visit(ctx.rhs)
        val rhs = pop()
        return push(functions.sub(lhs, rhs))
    }

    override fun visitText(ctx: JSongParser.TextContext): JsonNode? {
        return push(TextNode(ctx.text.substring(1, ctx.text.length - 1)))
    }

    override fun visitVariable(ctx: JSongParser.VariableContext): JsonNode? {
        return push(variables.peek()[ctx.LABEL().text])
    }

    override fun visitVariableBinding(ctx: JSongParser.VariableBindingContext): JsonNode? {
        val label = ctx.LABEL().text
        visit(ctx.exp())
        val exp = pop()
        variables.peek()[label] = exp
        return push(exp)
    }

    override fun visitWildcard(ctx: JSongParser.WildcardContext): JsonNode? {
        val exp = mapper.createArrayNode()
        functions.array(pop()).forEach { element ->
            element.fields().forEach { property ->
                exp.add(property.value)
            }
        }
        return push(exp)
    }

} //~ Processor
