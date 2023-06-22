package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Lexer
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class JSong(
   expr: String
) {

    private val parser: JSong2Parser = JSong2Parser(CommonTokenStream(JSong2Lexer(CharStreams.fromString(expr))))

    fun evaluate(
        node: JsonNode? = null,
        mapper: ObjectMapper = ObjectMapper()
    ): JsonNode? {
        val root = node?.let { ResultSequence().add(Context(it)) } ?: ResultSequence()
        return Processor(root, mapper).visit(parser.jsong()).value()
    }

} //~ JSong
