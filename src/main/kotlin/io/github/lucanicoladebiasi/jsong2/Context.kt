package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode

data class Context(
    val node: JsonNode,
    val prev: Context? = null
)