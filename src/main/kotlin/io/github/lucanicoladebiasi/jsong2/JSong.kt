package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Lexer
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.math.MathContext

class JSong(
    val expression: String,
    val mapper: ObjectMapper = ObjectMapper(),
    val mc: MathContext = MathContext.DECIMAL128
) {

    private val parser = JSong2Parser(CommonTokenStream(JSong2Lexer(CharStreams.fromString(expression))))

    fun evaluate(node: JsonNode? = null): JsonNode? {
        return Processor(node, mapper, mc).visit(parser.jsong())
    }

} //~ JSong