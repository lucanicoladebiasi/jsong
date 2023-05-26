package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Lexer
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class JSong(
    private val parser: JSong2Parser
) {

    companion object {

        fun expression(
            expr: String
        ): JSong {
            return JSong(JSong2Parser(CommonTokenStream(JSong2Lexer(CharStreams.fromString(expr)))))
        }

    } //~ companion

    fun evaluate(
        node: JsonNode? = null,
        mapr: ObjectMapper = ObjectMapper()
    ): JsonNode? {
        return Processor(node, mapr).visit(parser.exp_to_eof()).value
    }

} //~ Jsong
