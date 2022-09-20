package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.antlr.JSongLexer
import org.jsong.antlr.JSongParser
import kotlin.random.Random


class JSong private constructor(
    private val mapper: ObjectMapper,
    private val random: Random,
    private val parser: JSongParser
) {

    companion object {

        fun of(exp: String, random: Random = Random.Default, mapper: ObjectMapper = ObjectMapper()): JSong {
            return JSong(mapper, random, JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(exp)))))
        }


    } //~ companion

    fun evaluate(node: JsonNode? = null): JsonNode? {
        return Processor(mapper, random, node).visit(parser.jsong())
    }

} //~ JSong