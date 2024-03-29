/**
 * MIT License
 *
 * Copyright (c) 2023 Luca Nicola Debiasi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.lucanicoladebiasi.jsong

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

/**
 * This class defines a JSONata [function](https://docs.jsonata.org/programming#functions).
 *
 * Immutable.
 *
 * @param args labels of the arguments.
 * @param body code of the function.
 * @param nf node factory used to create this [FunctionNode],
 * by default set to the [JsonNodeFactory] of a new [ObjectNode].
 */
class FunctionNode(
    args: List<String>,
    body: String,
    nf: JsonNodeFactory = ObjectMapper().nodeFactory
) : ObjectNode(
    nf,
    mapOf(
        Pair(ARGS_TAG, nf.arrayNode().addAll(args.map { arg -> TextNode(arg) })),
        Pair(BODY_TAG, TextNode(body))
    )
) { //~ FunNode

    companion object {

        /**
         * Tag of the [args] property of this [FunctionNode] represented as an
         * [com.fasterxml.jackson.databind.node.ArrayNode] of [TextNode].
         */
        const val ARGS_TAG: String = "args"

        /**
         * Tag of the [body] property of this [FunctionNode] represented as [TextNode].
         */
        const val BODY_TAG = "body"

    } //~ companion

    /**
     * @property args arguments of this function.
     */
    val args get() = this[ARGS_TAG].map { arg -> arg.textValue() }

    /**
     * @property body code of the function.
     */
    val body get() = (this[BODY_TAG] as TextNode).textValue()

    /**
     * Return `true` if this object represents the same function of [other].
     * Two [FunctionNode] represents the same function if the [args] and [body]s are equals.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as FunctionNode

        if (args != other.args) return false
        if (body != other.body) return false

        return true
    }

    /**
     * See [Any.hashCode].
     */
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + args.hashCode()
        result = 31 * result + (body?.hashCode() ?: 0)
        return result
    }

    /**
     * Return the text describing this function node in the form
     *
     * `fun(arg[, arg]) { body }`.
     */
    override fun toString(): String {
        return "fun${args.joinToString(", ", "(", ")")} { $body }"
    }

} //~ FunctionNode