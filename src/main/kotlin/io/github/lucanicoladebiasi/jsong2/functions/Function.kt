package io.github.lucanicoladebiasi.jsong2.functions

import kotlin.reflect.KCallable

data class Function(val instance: Any, val callable: KCallable<*>) {

}
