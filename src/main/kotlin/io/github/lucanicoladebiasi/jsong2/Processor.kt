package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.lucanicoladebiasi.jsong1.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong1.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils

class Processor(
    root: JsonNode?,
    mapr: ObjectMapper
) : JSong2BaseVisitor<SequenceNode>() {

    companion object {

        fun sanitise(
            txt: String
        ): String {
            return StringEscapeUtils.unescapeJson(
                when {
                    txt.startsWith("`") && txt.endsWith("`")
                            || txt.startsWith("'") && txt.endsWith("''")
                            || txt.startsWith("\"") && txt.endsWith("\"") -> {
                        txt.substring(1, txt.length - 1)
                    }

                    else -> txt
                }
            )
        }


    } //~ companion

    private val nf = mapr.nodeFactory

    private val stack = ArrayDeque<SequenceNode>()

    private val trace = ArrayDeque<SequenceNode>()

    init {
        push(SequenceNode(nf).append(root))
    }

    private fun descendants(
        node: JsonNode?
    ): SequenceNode {
        val res = SequenceNode(nf)
        when(node) {
            null -> return res
            is ArrayNode -> {
                node.forEach {
                    res.addAll(descendants(it))
                }
            }
            is ObjectNode -> {
                res.add(node)
                node.fields().forEach {
                    res.addAll(descendants(it.value))
                }
            }
            else -> {
                res.add(node)
            }
        }
        return res
    }

    private fun pop(): SequenceNode {
        return stack.removeFirstOrNull() ?: SequenceNode(nf)
    }

    private fun push(
        seq: SequenceNode
    ): SequenceNode {
        stack.addFirst(seq)
        return seq
    }

    private fun select(
        seq: SequenceNode,
        offset: Int
    ): SequenceNode {
        val res = SequenceNode(nf)
        seq.forEach { context ->
            val size = context.size()
            val index = if (offset < 0) size + offset else offset
            if (index in 0 until size) {
                res.append(context[index])
            }
        }
        return res
    }

    private fun select(
        seq: SequenceNode,
        key: String
    ): SequenceNode {
        val res = SequenceNode(nf)
        seq.forEach { context ->
            context.filterIsInstance<ObjectNode>().filter { node ->
                node.has(key)
            }.forEach { node ->
                res.append(node[key])
            }
        }
        return res
    }

    private fun trace() {
        stack.firstOrNull()?.let {
            trace.add(it)
        }
    }

    override fun visitArray(
        ctx: JSong2Parser.ArrayContext
    ): SequenceNode {
        visit(ctx.exp())
        val rhs = pop()
        val lhs = pop()
        val res = when (lhs.isEmpty) {
            true -> rhs
            else -> when (val offset = rhs.value) {
                is NumericNode -> select(lhs, offset.asInt())
                else -> TODO()
            }
        }
        return push(res)
    }

    override fun visitBoolean(
        ctx: JSong2Parser.BooleanContext
    ): SequenceNode {
        val literal = when(ctx.literal.type) {
            JSong2Parser.TRUE -> BooleanNode.TRUE
            else -> BooleanNode.FALSE
        }
        return push(SequenceNode(nf).append(literal))
    }

    override fun visitDescendants(
        ctx: JSong2Parser.DescendantsContext
    ): SequenceNode {
        trace()
        return push(descendants(pop()))
    }

    override fun visitExp_to_eof(
        ctx: JSong2Parser.Exp_to_eofContext
    ): SequenceNode {
        super.visitExp_to_eof(ctx)
        return pop()
    }

    override fun visitField_values(
        ctx: JSong2Parser.Field_valuesContext
    ): SequenceNode {
        trace()
        val res = SequenceNode(nf)
        pop().flatten.forEach { node ->
            when (node) {
                is ObjectNode -> node.fields().forEach { field ->
                    res.append(field.value)
                }

                else -> res.append(node)
            }
        }
        return push(res)
    }

    override fun visitGoto(
        ctx: JSong2Parser.GotoContext
    ): SequenceNode {
        trace()
        val res = SequenceNode(nf)
        pop().flatten.forEach {
            val expr = "${ctx.exp().text}`${it.asText()}`"
            val eval = JSong.expression(expr).evaluate(trace.first())
            res.append(eval)
        }
        return push(res)
    }

    override fun visitId(
        ctx: JSong2Parser.IdContext
    ): SequenceNode {
        trace()
        return push(select(pop(), sanitise(ctx.ID().text)))
    }

    @Throws(JSongParseException::class)
    override fun visitNegative(
        ctx: JSong2Parser.NegativeContext
    ): SequenceNode {
        visit(ctx.exp())
        val res = when (val value = pop().value) {
            is NumericNode -> SequenceNode(nf).append(DecimalNode(value.decimalValue().negate()))
            else -> throw JSongParseException(ctx, "${ctx.text} not a number")
        }
        return push(res)
    }

    override fun visitNull(
        ctx: JSong2Parser.NullContext
    ): SequenceNode {
        return push(SequenceNode(nf).append(NullNode.instance))
    }

    override fun visitNumber(
        ctx: JSong2Parser.NumberContext
    ): SequenceNode {
        return push(SequenceNode(nf).append(DecimalNode(ctx.NUMBER().text.toBigDecimal())))
    }

    override fun visitObject(
        ctx: JSong2Parser.ObjectContext
    ): SequenceNode {
        val obj = ObjectNode(nf)
        ctx.field().forEachIndexed { index, field ->
            visit(field.key)
            val key = pop().value?.asText() ?: index.toString()
            visit(field.value)
            val value = pop().value ?: NullNode.instance
            obj.set<JsonNode>(key, value)
        }
        return push(SequenceNode(nf).append(obj))
    }

    override fun visitPath(
        ctx: JSong2Parser.PathContext
    ): SequenceNode {
        trace()
        return push(select(pop(), sanitise(ctx.ID().text)))
    }

    override fun visitParent(
        ctx: JSong2Parser.ParentContext
    ): SequenceNode {
        val index = trace.size - ctx.MODULE().size
        if (index < 0) throw JSongOutOfBoundsException(ctx, "${ctx.text} out of bound")
        val pop = pop().flatten
        val rec = trace[index].flatten
        val res = SequenceNode(nf)
        val ratio = rec.size().toDouble() / pop.size().toDouble()
        repeat(pop.size()) {
            val e = rec[(ratio * it).toInt()]
            res.append(e)
        }
        return push(res)
    }

    override fun visitString(
        ctx: JSong2Parser.StringContext
    ): SequenceNode {
        return push(SequenceNode(nf).append(TextNode(sanitise(ctx.text))))
    }

} //~ Processor