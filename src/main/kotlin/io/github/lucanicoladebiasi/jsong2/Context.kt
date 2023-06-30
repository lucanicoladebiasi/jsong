package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode

class Context(
    val node: JsonNode,
    val prev: Context? = null,
) {

//    operator fun get(fieldName: String): Context? {
//        if (node is ObjectNode && node.has(fieldName)) {
//            return Context(node[fieldName], this)
//        }
//        return null
//    }



    operator fun get(index: Int): Context? {
        when(node) {
            is ArrayNode -> {
                val offset = if (index < 0) node.size() + index else index
                if (offset in 0 until node.size()) {
                    return Context(node[offset], this)
                }
            }
            else -> if (index == 0) {
                return Context(node)
            }
        }
        return null
    }



} //~ Context