package io.github.lucanicoladebiasi.jsong2.functions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

class MatchNode(
    match: TextNode,
    index: IntNode,
    groups: ArrayNode,
    nf: JsonNodeFactory
) : ObjectNode(nf, mapOf(Pair(MATCH_TAG, match), Pair(INDEX_TAG, index), Pair(GROUPS_TAG, groups))) {

    companion object {

        const val MATCH_TAG = "match"

        const val INDEX_TAG = "index"

        const val GROUPS_TAG = "groups"

        fun of(
            match: String,
            index: Int,
            groups: Collection<String>,
            mapper: ObjectMapper = ObjectMapper()
        ): MatchNode {
            return MatchNode(
                TextNode(match),
                IntNode(index),
                mapper.createArrayNode().addAll(groups.map { TextNode(it) }),
                mapper.nodeFactory
            )
        }

    } //~ companion

    @Suppress("unused")
    val groups get() = this[GROUPS_TAG] as ArrayNode

    val index get() = this[INDEX_TAG] as IntNode

    @Suppress("unused")
    val match get() = this[MATCH_TAG] as TextNode

}