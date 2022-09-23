package org.jsong

import java.util.ArrayDeque

internal class Register {

    private val scope = ArrayDeque<MutableMap<String, Any>>()

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
            is Unit -> null
            else -> ref
        }
    }

    fun store(name: String, value: Any?): Register {
        scope.peek()[name] = value ?: Unit
        return this
    }

}