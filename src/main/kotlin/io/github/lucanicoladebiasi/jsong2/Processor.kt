package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NumericNode
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils

class Processor(
    private val root: ResultSequence,
) : JSong2BaseVisitor<ResultSequence>() {

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


    private val stack = Stack()

    init {
        stack.push(root)
    }

    override fun visitExp_to_eof(ctx: JSong2Parser.Exp_to_eofContext): ResultSequence {
        ctx.exp().forEach {
            visit(it)
        }
        return stack.pop()
    }


    override fun visitFilter(ctx: JSong2Parser.FilterContext): ResultSequence {
        visit(ctx.lhs)
        visit(ctx.rhs)
        when (val predicate = stack.pop().value()) {
            is NumericNode -> {
                stack.push(stack.pop().filter(predicate.asInt()))
            }

            else -> TODO()
        }
        return stack.peek()
    }

    override fun visitMap(ctx: JSong2Parser.MapContext): ResultSequence {
        val rs = ResultSequence()
        stack.pop().forEach { lhs ->
            stack.push(ResultSequence().add(lhs))
            visit(ctx.exp())
            stack.pop().forEach { rhs ->
                rs.add(Context(rhs.node, lhs))
            }
        }
        return stack.push(rs)
    }

    override fun visitNumber(ctx: JSong2Parser.NumberContext): ResultSequence {
        val digits = ctx.NUMBER().text
        val rs = ResultSequence().add(
            Context(
                DecimalNode(
                    when (ctx.SUB() == null) {
                        true -> digits.toBigDecimal()
                        else -> digits.toBigDecimal().negate()
                    }
                )
            )
        )
        return stack.push(rs)
    }

    override fun visitParent(ctx: JSong2Parser.ParentContext): ResultSequence {
        val steps = ctx.MODULE().size
        return stack.push(stack.pop().back(steps))
    }

    override fun visitSelect(ctx: JSong2Parser.SelectContext): ResultSequence {
        val fieldName = sanitise(ctx.ID().text)
        return stack.push(stack.pop().select(fieldName))
    }

}