package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.antlr.JSongLexer
import org.jsong.antlr.JSongParser
import java.time.Instant
import java.time.Instant.now
import kotlin.random.Random


class JSong private constructor(
    private val parser: JSongParser,
    private val mapper: ObjectMapper,
    private val random: Random,
    val time: Instant

) {

    companion object {

        fun of(
            exp: String,
            mapper: ObjectMapper = ObjectMapper(),
            random: Random = Random.Default,
            time: Instant = now()
        ): JSong {
            return JSong(JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(exp)))), mapper, random, time)
        }


    } //~ companion

    fun evaluate(node: JsonNode? = null): JsonNode? {
        return Processor(mapper, random, time, node).visit(parser.jsong())
    }

} //~ JSong