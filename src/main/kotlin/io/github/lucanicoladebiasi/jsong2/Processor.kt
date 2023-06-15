package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils

class Processor(
    private val root: Sequence
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

    override fun visitMap(ctx: JSong2Parser.MapContext): Sequence {
        visit(ctx.exp())
        return stack.peek()
    }

    override fun visitSelect(ctx: JSong2Parser.SelectContext): Sequence {
        val fieldName = sanitise(ctx.ID().text)
        val result = stack.pop().get(fieldName)
        return stack.push(result)
    }

}