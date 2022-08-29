package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.antlr.JSongLexer
import org.jsong.antlr.JSongParser

class JSong private constructor(
    private val mapper: ObjectMapper,
    private val parser: JSongParser
) {

    companion object {

        fun of(exp: String, mapper: ObjectMapper = ObjectMapper()): JSong {
            return JSong(mapper, JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(exp)))))
        }


    } //~ companion

    fun evaluate(node: JsonNode? = null): JsonNode? {
        return Processor(mapper, node).visit(parser.jsong())
    }

} //~ JSong