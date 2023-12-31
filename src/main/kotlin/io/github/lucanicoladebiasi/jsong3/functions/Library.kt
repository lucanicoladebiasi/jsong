package io.github.lucanicoladebiasi.jsong3.functions

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.memberFunctions

open class Library(val exportPrefix: String = "$") {


    fun <T : Any> register(map: MutableMap<String, MutableMap<List<KType>, KFunction<*>>>, `class`: KClass<T>) {
        `class`.memberFunctions
            .filter { it.name.startsWith(exportPrefix) }
            .sortedWith { f1, f2 ->
                when (val n = f1.name.compareTo(f2.name)) {
                    0 -> f1.parameters.size.compareTo(f2.parameters.size)
                    else -> n
                }
            }.forEach { f ->
                val signature = f.parameters.map { p -> p.type }
                val functions = map.getOrDefault(f.name, mutableMapOf())
                functions[signature] = f
                map[f.name] = functions
            }
    }

}

fun main() {
    val map = mutableMapOf<String, MutableMap<List<KType>, KFunction<*>>>()
    Library().register(map, NumericFunctions::class)
    map.forEach { (t, u) ->
        println(t)
        println(u)
    }
}

