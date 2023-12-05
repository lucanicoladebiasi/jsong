package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.math.MathContext

data class Context(
    val node: JsonNode?,
    val loop: Int?,
    val mapper: ObjectMapper,
    val mathContext: MathContext,
    val variables: MutableMap<String, JsonNode>
)