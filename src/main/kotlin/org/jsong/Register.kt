package org.jsong

import com.fasterxml.jackson.databind.node.ArrayNode
import java.util.ArrayDeque

class Register {

    private val scope = ArrayDeque<MutableMap<String, ArrayNode>>()

    var index: Int? = null

    init {
        push()
    }

    fun pop(): Register {
        if (scope.size > 1) {
            scope.pop()
        }
        return this
    }

    fun push(): Register {
        scope.push(mutableMapOf())
        return this
    }

    fun recall(name: String): Any? {
        return when (val ref = scope.peek()[name]) {
            is ArrayNode -> when (index) {
                null -> ref
                else -> ref[index!!]
            }
            else -> ref
        }
    }

    fun store(name: String, value: ArrayNode): Register {
        scope.peek()[name] = value
        return this
    }

}