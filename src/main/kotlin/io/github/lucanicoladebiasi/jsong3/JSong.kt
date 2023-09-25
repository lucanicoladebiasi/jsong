package io.github.lucanicoladebiasi.jsong3

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
    val mathContext: MathContext = MathContext.DECIMAL128,
    val variables: MutableMap<String, JsonNode> = mutableMapOf()
) {

    private val parser = JSong2Parser(CommonTokenStream(JSong2Lexer(CharStreams.fromString(expression))))

    fun evaluate(node: JsonNode? = null): JsonNode? {
        return Visitor(node, null, mapper, mathContext, variables).visit(parser.jsong())
    }

} //~ JSong