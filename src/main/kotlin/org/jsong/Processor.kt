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

    init {
        push(root)
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
            is RangesNode -> {
                val array = functions.array(node)
                path.indexes.forEach {
                    val value = it.asInt()
                    val index = if (value < 0) array.size() + value else value
                    array.get(index)?.let { exp.add(it) }
                }
            }
            is ArrayNode -> path.forEach {
                exp.addAll(select(it, node))
            }
            is NumericNode -> {
                val array = functions.array(node)
                val value = path.asInt()
                val index = if (value < 0) array.size() + value else value
                array.get(index)?.let { exp.add(it) }
            }
            is PathNode -> when (node) {
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
        visit(ctx.exp())
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

    override fun visitJsong(ctx: JSongParser.JsongContext): JsonNode? {
        ctx.children.forEach {
            visit(it)
        }
        return functions.flatten(stack.firstOrNull())
    }

//    override fun visitCompare(ctx: JSongParser.CompareContext): JsonNode? {
//        visit(ctx.exp())
//
//        val rhs = pop()
//        val res = when {
//            ctx.GE() != null -> functions.ge(lhs, rhs)
//            ctx.GT() != null -> functions.gt(lhs, rhs)
//            ctx.EQ() != null -> functions.eq(lhs, rhs)
//            ctx.LE() != null -> functions.le(lhs, rhs)
//            ctx.LT() != null -> functions.lt(lhs, rhs)
//            ctx.NE() != null -> functions.ne(lhs, rhs)
//            ctx.IN() != null -> functions.include(lhs, rhs)
//            else -> throw UnsupportedOperationException("$ctx not recognized")
//        }
//        return push(res)
//    }

    override fun visitFilter(ctx: JSongParser.FilterContext): JsonNode? {
        visit(ctx.exp())
        val path = pop()
        return push(when(val node = pop()) {
            null -> node
            else -> select(node, path)
        })
    }

    override fun visitMap(ctx: JSongParser.MapContext): JsonNode? {
        val exp = mapper.createArrayNode()
        functions.array(pop()).forEach { node ->
            push(node)
            visit(ctx.exp())
            if (ctx.filter() != null) {
                visit(ctx.filter())
            }
            pop()?.let { exp.addAll(functions.array(it)) }
        }
        return push(exp)
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

    override fun visitScope(ctx: JSongParser.ScopeContext): JsonNode? {
        ctx.children.forEach {
            visit(it)
        }
        return stack.firstOrNull()
    }

    override fun visitText(ctx: JSongParser.TextContext): JsonNode? {
        return push(TextNode(ctx.text.substring(1, ctx.text.length - 1)))
    }

} //~ Processor
