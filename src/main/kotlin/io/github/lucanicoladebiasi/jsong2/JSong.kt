package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Lexer
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class JSong(
    val expression: String,
    val mapper: ObjectMapper = ObjectMapper()
) {

    private val parser = JSong2Parser(CommonTokenStream(JSong2Lexer(CharStreams.fromString(expression))))

    fun process(node: JsonNode? = null): JsonNode? {
        return Processor(node, mapper).visit(parser.jsong())
    }

    fun evaluate(node: JsonNode? = null): JsonNode? {
        return Processor.reduce(process(node))
    }

} //~ JSong