package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils

class Processor(
    private val root: Sequence,
) : JSong2BaseVisitor<Sequence>() {

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

    override fun visitExp_to_eof(ctx: JSong2Parser.Exp_to_eofContext): Sequence {
        ctx.exp().forEach {
            visit(it)
        }
        return stack.pop()
    }

//    override fun visitFilter(ctx: JSong2Parser.FilterContext): Sequence {
//        visit(ctx.lhs)
//        visit(ctx.rhs)
//        val predicate = stack.pop().json(mapper)
//        val result = when {
//            predicate == null -> Sequence()
//            predicate.isNumber -> {
//                stack.pop().filter(predicate.asInt())
//            }
//            else -> Sequence()
//        }
//        return stack.push(result)
//    }

//    override fun visitMap(ctx: JSong2Parser.MapContext): Sequence {
//        visit(ctx.exp())
//        return stack.peek()
//    }

//    override fun visitNumber(ctx: JSong2Parser.NumberContext): Sequence {
//        var value = ctx.NUMBER().text.toBigDecimal()
//        if (ctx.SUB() != null) {
//            value = value.negate()
//        }
//        return stack.push(Sequence().add(Context(DecimalNode(value))))
//    }

    override fun visitMap(ctx: JSong2Parser.MapContext): Sequence {
        val sequence = Sequence()
        stack.pop().forEach { context ->
            stack.push(Sequence().add(context))
            visit(ctx.exp())
            stack.pop().forEach {
                sequence.add(it)
            }
        }
        return stack.push(sequence)
    }

    override fun visitSelect(ctx: JSong2Parser.SelectContext): Sequence {
        val fieldName = sanitise(ctx.ID().text)
        val sequence = Sequence()
        stack.pop().filter { context ->
            context.node is ObjectNode && context.node.has(fieldName)
        }.forEach { context ->
            sequence.add(Context(context.node[fieldName]))
        }
        return stack.push(sequence)
    }

}