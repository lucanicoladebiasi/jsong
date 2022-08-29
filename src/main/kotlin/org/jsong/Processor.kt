package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.jsong.antlr.JSongBaseVisitor
import org.jsong.antlr.JSongParser
import java.util.*

class Processor internal constructor(
    private val mapper: ObjectMapper,
    private val root: JsonNode?
) : JSongBaseVisitor<JsonNode?>() {

    private val functions = Functions()

    private val stack = ArrayDeque<JsonNode>()

    init {
        push(root)
    }

    private fun array(node: JsonNode?): ArrayNode {
        return when (node) {
            null -> mapper.createArrayNode()
            is ArrayNode -> node
            else -> mapper.createArrayNode().add(node)
        }
    }

    private fun flatten(node: JsonNode?): JsonNode? {
        return when (node) {
            is ArrayNode -> when (node.size()) {
                0 -> null
                1 -> flatten(node[0])
                else -> {
                    val res = mapper.createArrayNode()
                    node.forEach { element ->
                        flatten(element)?.let { res.add(it) }
                    }
                    res
                }
            }
            else -> node
        }
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

    private fun select(path: JsonNode?, node: JsonNode?): ArrayNode {
        val exp = mapper.createArrayNode()
        val key = path?.asText()
        if (!key.isNullOrEmpty()) when (node) {
            is ArrayNode -> node.forEach { element ->
                exp.add(select(path, element))
            }
            is ObjectNode -> node[key]?.let { exp.add(it) }
        }
        return exp
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
                else -> throw IllegalArgumentException("${ctx.text} not recognized")
            }
        )
    }

    override fun visitFilter(ctx: JSongParser.FilterContext): JsonNode? {
        val exp = mapper.createArrayNode()
        val lhs = array(pop())
        lhs.forEach { element ->
            array(element).forEachIndexed { index, node ->
                push(node)
                visit(ctx.exp())
                when (val rhs = pop()) {
                    is DecimalNode -> if (offset(lhs, rhs.asInt()) == index) exp.add(node)
                    else -> if (functions.boolean(rhs).booleanValue()) exp.add(node)
                }
            }
        }
        return push(exp)
    }

    override fun visitJsong(ctx: JSongParser.JsongContext): JsonNode? {
        ctx.exp().forEach {
            visit(it)
        }
        return flatten(stack.firstOrNull())
    }

    override fun visitMap(ctx: JSongParser.MapContext): JsonNode? {
        val exp = mapper.createArrayNode()
        val lhs = array(flatten(pop()))
        lhs.forEach { node ->
            push(node)
            visit(ctx.exp())
            pop()?.let { exp.add(it) }
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
        for (i in 0 until ctx.exp().size step 2) {
            val name = visit(ctx.exp(i))?.asText() ?: i.div(2).toString()
            val value = visit(ctx.exp(i + 1)) ?: NullNode.instance
            exp.set<JsonNode>(name, value)
        }
        return push(exp)
    }

    override fun visitPath(ctx: JSongParser.PathContext): JsonNode? {
        var exp = push(PathNode(ctx.text))
        if (stack.size > 1) {
            exp = push(select(pop(), pop()))
        }
        return exp
    }

    override fun visitText(ctx: JSongParser.TextContext): JsonNode? {
        return push(TextNode(ctx.text.substring(1, ctx.text.length - 1)))
    }

}