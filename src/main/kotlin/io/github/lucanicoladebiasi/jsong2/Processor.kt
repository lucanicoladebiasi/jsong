package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
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
                            || txt.startsWith("'") && txt.endsWith("'")
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

    init {
        push(SequenceNode(nf).append(root))
    }

    private fun descendants(
        node: JsonNode?
    ): SequenceNode {
        val result = SequenceNode(nf)
        when(node) {
            null -> return result
            is ArrayNode -> {
                node.forEach {
                    result.addAll(descendants(it))
                }
            }
            is ObjectNode -> {
                result.add(node)
                node.fields().forEach {
                    result.addAll(descendants(it.value))
                }
            }
            else -> {
                result.add(node)
            }
        }
        return result
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
        key: String
    ): SequenceNode {
        val result = SequenceNode(nf)
        seq.forEach { context ->
            context.filterIsInstance<ObjectNode>().filter { node ->
                node.has(key)
            }.forEach { node ->
                result.append(node[key])
            }
        }
        return result
    }

    override fun visitBoolean(
        ctx: JSong2Parser.BooleanContext
    ): SequenceNode {
        val boolean = when(ctx.TRUE() != null) {
            true -> BooleanNode.TRUE
            else -> BooleanNode.FALSE
        }
        return push(SequenceNode(nf).append(boolean))
    }

    override fun visitDescendants(
        ctx: JSong2Parser.DescendantsContext
    ): SequenceNode {
        return push(descendants(pop()))
    }

    override fun visitExp_to_eof(
        ctx: JSong2Parser.Exp_to_eofContext
    ): SequenceNode {
        super.visitExp_to_eof(ctx)
        return pop()
    }

    override fun visitFields(
        ctx: JSong2Parser.FieldsContext
    ): SequenceNode {
        val result = SequenceNode(nf)
        pop().flatten.forEach { node ->
            when (node) {
                is ObjectNode -> node.fields().forEach { field ->
                    result.append(field.value)
                }

                else -> result.append(node)
            }
        }
        return push(result)
    }

    override fun visitGoto(
        ctx: JSong2Parser.GotoContext
    ): SequenceNode {
        val res = SequenceNode(nf)
        pop().flatten.forEach {
            val expr = "${ctx.exp().text}`${it.asText()}`"
            val eval = JSong.expression(expr).evaluate(root)
            res.append(eval)
        }
        return push(res)
    }

    @Throws(
        NotNumericException::class,
    )
    override fun visitIndex(
        ctx: JSong2Parser.IndexContext
    ): SequenceNode {
        visit(ctx.exp())
        val offset = pop().value?.asInt() ?: throw NotNumericException(ctx, "${ctx.exp().text} not a number")
        val result = SequenceNode(nf)
        pop().forEach { context ->
            when(context) {
                is ArrayNode -> {
                    val size = context.size()
                    val index = if (offset < 0) size + offset else offset
                    if (index in 0 until size) {
                        result.append(context[index])
                    }
                }
            }
        }
        return push(result)
    }

    @Throws(
        NotNumericException::class
    )
    override fun visitNegative(
        ctx: JSong2Parser.NegativeContext
    ): SequenceNode {
        visit(ctx.exp())
        val context = pop()
        val n = context.value?.decimalValue()?.negate() ?: throw NotNumericException(ctx, "${ctx.text} not a number")
        return push(SequenceNode(nf).append(DecimalNode(n)))
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

    override fun visitSelect(
        ctx: JSong2Parser.SelectContext
    ): SequenceNode {
        return push(select(pop(), sanitise(ctx.ID().text)))
    }


//    override fun visitParent(
//        ctx: JSong2Parser.ParentContext
//    ): SequenceNode {
//        val index = trace.size - ctx.MODULE().size
//        if (index < 0) throw JSongOutOfBoundsException(ctx, "${ctx.text} out of bound")
//        val pop = pop().flatten
//        val rec = trace[index].flatten
//        val res = SequenceNode(nf)
//        val ratio = rec.size().toDouble() / pop.size().toDouble()
//        repeat(pop.size()) {
//            val e = rec[(ratio * it).toInt()]
//            res.append(e)
//        }
//        return push(res)
//    }

    override fun visitString(
        ctx: JSong2Parser.StringContext
    ): SequenceNode {
        return push(SequenceNode(nf).append(TextNode(sanitise(ctx.text))))
    }

} //~ Processor