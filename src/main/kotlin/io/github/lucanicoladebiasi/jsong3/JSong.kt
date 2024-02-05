package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Lexer
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Parser
import io.github.lucanicoladebiasi.jsong3.functions.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.math.MathContext
import java.time.Instant
import kotlin.random.Random

class JSong(
    val expression: String,
    private val lib: Library = Library(),
    private val mc: MathContext = MathContext.DECIMAL128,
    private val now: Instant = Instant.now(),
    private val om: ObjectMapper = ObjectMapper(),
    private val rand: Random = Random.Default,
    private val vars: MutableMap<String, JsonNode?> = mutableMapOf()
) {

    init {
        lib.register(ArrayFunctions(lib, mc, now, om, rand, vars))
            .register(BooleanFunctions)
            .register(DateTimeFunctions(now))
            .register(NumericFunctions(mc, rand))
            .register(NumericAggregationFunctions(mc))
            .register(ObjectFunctions(om))
    }

    private val parser = JSong3Parser(CommonTokenStream(JSong3Lexer(CharStreams.fromString(expression))))

    fun evaluate(node: JsonNode? = null): JsonNode? {
        return Visitor(Context(lib, null, mc, node, now, om, mutableMapOf(), rand, vars)).visit(parser.jsong())
    }

} //~ JSong