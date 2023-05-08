package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.lucanicoladebiasi.jsong1.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong1.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils

class Processor(
    private val root: JsonNode?,
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
        stack.firstOrNull()?.let { trace.addFirst(it) }
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
        stack.firstOrNull()?.let { trace.addFirst(it) }
        val res = SequenceNode(nf)
        seq.forEach { context ->
            context.filterIsInstance<ObjectNode>().filter { node ->
                node.has(key)
            }.forEach { node ->
                res.append(node[key])
            }
        }
        trace.add(res)
        return res
    }

    override fun visitArray(
        ctx: JSong2Parser.ArrayContext
    ): SequenceNode {
        visit(ctx.exp())
        val rhs = pop()
        val lhs = pop()
        val res = when(lhs.isEmpty) {
            true -> rhs
            else -> when(val offset = rhs.value) {
                is NumericNode -> select(lhs, offset.asInt())
                else -> TODO()
            }
        }
        return push(res)
    }

    override fun visitExp_to_eof(
        ctx: JSong2Parser.Exp_to_eofContext
    ): SequenceNode {
        super.visitExp_to_eof(ctx)
        return pop()
    }

    override fun visitId(
        ctx: JSong2Parser.IdContext
    ): SequenceNode {
        return push(select(pop(), sanitise(ctx.ID().text)))
    }

    @Throws(JSongParseException::class)
    override fun visitNegative(
        ctx: JSong2Parser.NegativeContext
    ): SequenceNode {
        visit(ctx.exp())
        val value = pop().value
        val res = when(value) {
            is NumericNode -> SequenceNode(nf).append(DecimalNode(value.decimalValue().negate()))
            else -> throw JSongParseException(ctx, "${ctx.text} not a number")
        }
        return push(res)
    }

    override fun visitNumber(
        ctx: JSong2Parser.NumberContext
    ): SequenceNode {
        return push(SequenceNode(nf).append(DecimalNode(ctx.NUMBER().text.toBigDecimal())))
    }

    override fun visitPath(
        ctx: JSong2Parser.PathContext
    ): SequenceNode {
        return push(select(pop(), sanitise(ctx.ID().text)))
    }

    override fun visitParent(
        ctx: JSong2Parser.ParentContext
    ): SequenceNode {
        val index = trace.size - 1 - ctx.PRC().size
        if (index < 0) throw JSongOutOfBoundsException(ctx, "${ctx.text} out of bound")
        val res = pop().value
        return push(SequenceNode(nf).append(res))
    }


} //~ Processor