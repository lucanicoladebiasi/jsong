/**
 * MIT License
 *
 * Copyright (c) [2023] [Luca Nicola Debiasi]
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
package dev.jsong

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

/**
 * This class represents an array of [RangeNode] objects.
 *
 * Immutable.
 *
 * @param nodeFactory the node factory used to create this object,
 * by default set the the [JsonNodeFactory] of a new [ObjectMapper].
 */
class RangesNode constructor(
    nodeFactory: JsonNodeFactory = ObjectMapper().nodeFactory
) : ArrayNode(nodeFactory) {

    /**
     * @property indexes array of integers between the lowest boundary and the higher of the represented ranges,
     * ranges are merged if overlapping.
     *
     * See the JSONata [range](https://docs.jsonata.org/numeric-operators#-range) operator.
     */
    val indexes: ArrayNode
        get() {
            val set = mutableSetOf<Int>()
            forEach { node ->
                when (node) {
                    is RangeNode -> node.indexes.forEach {
                        when (it) {
                            is IntNode -> set.add(it.asInt())
                        }
                    }
                }
            }
            return _nodeFactory.arrayNode().addAll(set.sorted().map { IntNode(it) })
        }

} //~ RangesNode