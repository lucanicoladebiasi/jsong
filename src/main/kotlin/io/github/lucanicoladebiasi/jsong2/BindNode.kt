package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

class BindNode(nf: JsonNodeFactory, pos: IntNode, value: JsonNode?) : ObjectNode(
    nf,
    mapOf(Pair(POS, pos), Pair(VAL, value))
) {

    companion object {

        const val POS = "pos"

        const val VAL = "val"
    }

    val pos: IntNode = this[POS] as IntNode

    val value: JsonNode? = this[VAL]

} //~ Bind