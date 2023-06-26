package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode

class Context(
    val node: JsonNode,
    val prev: JsonNode? = null,
)