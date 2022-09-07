package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.jsong.antlr.JSongBaseVisitor
import org.jsong.antlr.JSongParser
import java.lang.UnsupportedOperationException
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

    private fun offset(node: ArrayNode, index: Int): Int {
        return when {
            index < 0 -> node.size() + index
            else -> index
        }
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

    private fun select(path: JsonNode?, node: JsonNode?): JsonNode? {
        val key = path?.asText()
        return when (key.isNullOrEmpty()) {
            true -> null
            else -> when (node) {
                is ObjectNode -> node[key]
                else -> null
            }
        }
    }

    override fun visitArray(ctx: JSongParser.ArrayContext): JsonNode? {
        val exp = mapper.createArrayNode()
        ctx.exp().forEach { expCtx ->
            exp.add(visit(expCtx))
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

    override fun visitJsong(ctx: JSongParser.JsongContext): JsonNode? {
        super.visitJsong(ctx)
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

//    override fun visitFilter(ctx: JSongParser.FilterContext): JsonNode? {
//        val exp = mapper.createArrayNode()
//        val lhs = pop()
//        functions.array(lhs).forEach { node ->
//            ctx.exp().forEach {
//                push(node)
//                visit(it)
//                val rhs = pop()
//                println(rhs)
//            }
//
//        }
//        return push(exp)
//    }

    //        ctx.exp().forEach {
//            push(lhs)
//            visit(it)
//        }
//        when (val rhs = pop()) {
//            is NumericNode -> {
//                lhs[offset(lhs, rhs.asInt())]?.let { exp.add(it) }
//            }
//            is RangeNodes -> {
//                rhs.indexes.forEach { index ->
//                    lhs[offset(lhs, index.asInt())]?.let { exp.add(it) }
//                }
//            }
//            else -> {
//                println("boo")
//            }
//        }

//    override fun visitMap(ctx: JSongParser.MapContext): JsonNode? {
//        val exp = mapper.createArrayNode()
//        functions.array(pop()).forEach { node ->
//            push(node)
//            visit(ctx.exp())
//            if (ctx.filter() != null) {
//                visit(ctx.filter())
//            }
//            pop()?.let { exp.add(it) }
//        }
//        return push(exp)
//    }

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

//    override fun visitPath(ctx: JSongParser.PathContext): JsonNode? {
//        var exp = push(PathNode(ctx.text))
//        if (stack.size > 1) {
//            exp = push(select(pop(), pop()))
//        }
//        return exp
//    }

    override fun visitRange(ctx: JSongParser.RangeContext): JsonNode? {
        visit(ctx.min)
        val min = stack.pop()
        visit(ctx.max)
        val max = stack.pop()
        return push(RangeNode.of(min.decimalValue(), max.decimalValue(), mapper.nodeFactory))
    }

    override fun visitRanges(ctx: JSongParser.RangesContext): JsonNode? {
        val exp = RangeNodes(mapper.nodeFactory)
        ctx.range().forEach {
            visit(it)
            exp.add(pop())
        }
        return push(exp)
    }

//    override fun visitScope(ctx: JSongParser.ScopeContext): JsonNode? {
//        ctx.exp().forEach { ctx_exp ->
//            visit(ctx_exp)
//        }
//        return stack.firstOrNull()
//    }

    override fun visitText(ctx: JSongParser.TextContext): JsonNode? {
        return push(TextNode(ctx.text.substring(1, ctx.text.length - 1)))
    }

} //~ Processor
