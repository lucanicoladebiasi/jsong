package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Lexer
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.math.MathContext

class JSong(
    val expression: String,
    private val variables: MutableMap<String, JsonNode> = mutableMapOf(),
    private val om: ObjectMapper = ObjectMapper(),
    private val mc: MathContext = MathContext.DECIMAL128
) {

    private val parser = JSong3Parser(CommonTokenStream(JSong3Lexer(CharStreams.fromString(expression))))

    fun evaluate(node: JsonNode? = null): JsonNode? {
        return Visitor(Context(node, null, mutableMapOf(), variables, om, mc)).visit(parser.jsong())
    }

} //~ JSong