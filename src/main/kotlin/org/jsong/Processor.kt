package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.jsong.antlr.JSongBaseVisitor
import org.jsong.antlr.JSongParser
import java.util.*

class Processor internal constructor(
    private val mapper: ObjectMapper,
    root: JsonNode?
) : JSongBaseVisitor<JsonNode?>() {
    private val functions = Functions(mapper)
    private val stack = ArrayDeque<JsonNode>()
    @Volatile
    private var isToFlatten = true

    init {
        push(root)
    }

    private fun push(node: JsonNode?): JsonNode? {
        if (node != null) stack.push(node)
        return node
    }

    private fun pop(): JsonNode? {
        return when(stack.isEmpty()) {
            true -> null
            else -> stack.pop()
        }
    }

    private fun select(node: JsonNode?, path: JsonNode?): ArrayNode {
        val exp = mapper.createArrayNode()
        when(path) {
            is RangesNode -> {
                val array = functions.array(node)
                path.indexes.forEach {
                    val value = it.asInt()
                    val index = if (value < 0) array.size() + value else value
                    array.get(index)?.let { exp.add(it) }
                }
            }

            is NumericNode -> {
                val array = functions.array(node)
                val value = path.asInt()
                val index = if (value < 0) array.size() + value else value
                array.get(index)?.let { exp.add(it) }
            }

            is PathNode -> when(node) {
                is ArrayNode -> node.forEach {
                    exp.addAll(select(it, path))
                }

                is ObjectNode -> node[path.asText()]?.let {
                    when(it) {
                        is ArrayNode -> exp.addAll(it)
                        else -> exp.add(it)
                    }
                }
            }
        }
        return exp
    }

    override fun visitAdd(ctx: JSongParser.AddContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.add(lhs, rhs))
    }

    override fun visitArray(ctx: JSongParser.ArrayContext): JsonNode? {
        val exp = mapper.createArrayNode()
        ctx.children.forEach {
            visit(it)
            pop()?.let { exp.add(it) }
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

    override fun visitContext(ctx: JSongParser.ContextContext): JsonNode? {
        return push(stack.firstOrNull())
    }

    override fun visitDiv(ctx: JSongParser.DivContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.div(lhs, rhs))
    }

    override fun visitEq(ctx: JSongParser.EqContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.eq(lhs, rhs))
    }

    override fun visitFilter(ctx: JSongParser.FilterContext): JsonNode? {
        val exp = mapper.createArrayNode()
        visit(ctx.lhs)
        val lhs = functions.array(pop())
        visit(ctx.rhs)
        when(val rhs = pop()) {
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
                    if (functions.boolean(pop()).booleanValue()) {
                        exp.add(element)
                    }
                }
            }
        }
        return push(exp)
    }

    override fun visitGt(ctx: JSongParser.GtContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.gt(lhs, rhs))
    }

    override fun visitGte(ctx: JSongParser.GteContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.gte(lhs, rhs))
    }

    override fun visitIn(ctx: JSongParser.InContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.include(lhs, rhs))
    }

    override fun visitJsong(ctx: JSongParser.JsongContext): JsonNode? {
        ctx.exp()?.let { visit(it) }
        return stack.firstOrNull()?.let { if (isToFlatten) functions.flatten(it) else it }
    }

    override fun visitLt(ctx: JSongParser.LtContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.lt(lhs, rhs))
    }

    override fun visitLte(ctx: JSongParser.LteContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.lte(lhs, rhs))
    }

    override fun visitMap(ctx: JSongParser.MapContext): JsonNode? {
        val exp = mapper.createArrayNode()
        visit(ctx.lhs)
        when(val lhs = pop()) {
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
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.mul(lhs, rhs))
    }

    override fun visitNe(ctx: JSongParser.NeContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.ne(lhs, rhs))
    }

    override fun visitNihil(ctx: JSongParser.NihilContext): JsonNode? {
        return push(NullNode.instance)
    }

    override fun visitNumber(ctx: JSongParser.NumberContext): JsonNode? {
        return push(DecimalNode(ctx.NUMBER().text.toBigDecimal()))
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
        visit(ctx.min)
        val min = stack.pop()
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
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.reminder(lhs, rhs))
    }

    override fun visitScope(ctx: JSongParser.ScopeContext): JsonNode? {
        ctx.children.forEach {
            visit(it)
        }
        return stack.firstOrNull()
    }

    override fun visitSub(ctx: JSongParser.SubContext): JsonNode? {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val rhs = pop()
        val lhs = pop()
        return push(functions.sub(lhs, rhs))
    }

    override fun visitText(ctx: JSongParser.TextContext): JsonNode? {
        return push(TextNode(ctx.text.substring(1, ctx.text.length - 1)))
    }
} //~ Processor
