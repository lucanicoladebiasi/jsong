package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.lucanicoladebiasi.jsong1.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong1.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils

class Processor(
    private val root: JsonNode?,
    private val mapr: ObjectMapper
) : JSong2BaseVisitor<Sequence>() {

    companion object {

        private const val PARENT_ID = "%"

        private const val MAP_ID = "."

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

    private val operands = Operands()

    init {
        operands.push(Sequence(nf).append(root))
    }

    override fun visitExp_to_eof(
        ctx: JSong2Parser.Exp_to_eofContext
    ): Sequence {
        super.visitExp_to_eof(ctx)
        return operands.pop()!!
    }

    override fun visitFilter(
        ctx: JSong2Parser.FilterContext
    ): Sequence {
        visit(ctx.exp())
        val predicate = operands.pop().value
        val pop = operands.pop()
        val result = Sequence(nf)
        if (predicate != null) {
            when {
                predicate.isNumber -> {
                    pop.forEach { context ->
                        val size = context.size()
                        val offset = predicate.asInt()
                        val index = if (offset < 0) size + offset else offset
                        if (index in 0 until size) {
                            result.append(context[index])
                        }
                    }
                }

                else -> TODO()
            }
        }
        return operands.push(result)
    }

    override fun visitMap(
        ctx: JSong2Parser.MapContext
    ): Sequence {
        val result = Sequence(nf)
        operands.pop().flatten.forEachIndexed { index, context ->
            operands.push(Sequence(nf).append(context))
            visit(ctx.exp())
            result.append(operands.pop())
        }
        return operands.push(result)
    }

    override fun visitNegative(
        ctx: JSong2Parser.NegativeContext
    ): Sequence {
        visit(ctx.exp())
        val context = operands.pop()
        val number =
            context.value?.decimalValue()?.negate() ?: throw NotNumericException(ctx, "${ctx.text} not a number")
        return operands.push(Sequence(nf).append(DecimalNode(number)))
    }

    override fun visitNumber(
        ctx: JSong2Parser.NumberContext
    ): Sequence {
        return operands.push(Sequence(nf).append(DecimalNode(ctx.NUMBER().text.toBigDecimal())))
    }

    override fun visitSelect(
        ctx: JSong2Parser.SelectContext
    ): Sequence {
        val result = Sequence(nf)
        val id = sanitise(ctx.ID().text)
        operands.pop().forEach { context ->
            context.filterIsInstance<ObjectNode>().filter { node ->
                node.has(id)
            }.forEach { node ->
                result.append(node[id])
            }
        }
        return operands.push(result)
    }


} //~ Processor