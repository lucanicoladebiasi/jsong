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
package io.github.lucanicoladebiasi.jsong1

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import java.math.BigDecimal

/**
 * This class represents a closed interval between [min] and [max] included,
 * represented as a JSON object having [MIN_TAG] and [MAX_TAG] as [DecimalNode] properties.
 *
 * The class extends the JSONata [range](https://docs.jsonata.org/numeric-operators#-range) notation
 * to include in the interval any [BigDecimal] value,
 * not limited to the sequence of internet between [min] and [max].
 *
 * Use [of] to create a new instance of this class.
 *
 * The [indexes] property returns the [ArrayNode] of [Int] elements as defined by the JSON
 * [range](https://docs.jsonata.org/numeric-operators#-range) notation.
 *
 * Immutable.
 *
 * @param min lower end of the represented closed interval.
 * @param max higher end of teh represented closed interval.
 * @param nf node factory used to create this object,
 *
 */
class RangeNode private constructor(
    min: DecimalNode,
    max: DecimalNode,
    nf: JsonNodeFactory
) : ObjectNode(nf, mapOf<String, DecimalNode>(Pair(MAX_TAG, max), Pair(MIN_TAG, min))) {

    companion object {

        /**
         * Tag of the [max] property of this range represented as [ObjectNode].
         */
        const val MAX_TAG = "max"

        /**
         * Tag of the [min] property of this range represented as [ObjectNode].
         */
        const val MIN_TAG = "min"

        /**
         * Return o new [RangesNode] object representing the closed interval between [x] and [y]:
         * [min] is set to the minimum between [x] or [y];
         * [max] is set to the maximum between [x] or [y].
         *
         * @param x boundary of the range, either [min] or [max].
         * @param y boundary of the range, either [min] or [max].
         * @param nf node factory used to create the new [RangesNode],
         * by default set to the [JsonNodeFactory] of a new [ObjectMapper].
         */
        fun of(
            x: BigDecimal,
            y: BigDecimal,
            nf: JsonNodeFactory = ObjectMapper().nodeFactory
        ): RangeNode {
            return RangeNode(DecimalNode(x.min(y)), DecimalNode(x.max(y)), nf)
        }

    } //~ companion

    /**
     * @property max the higher boundary of this range.
     */
    val max get() = this[MAX_TAG] as DecimalNode

    /**
     * @property min lower boundry of this range.
     */
    val min get() = this[MIN_TAG] as DecimalNode

    /**
     * @property indexes array of integers between [min] and [max] included as defined by the JSONata
     * [range](https://docs.jsonata.org/numeric-operators#-range) operator.
     */
    val indexes: ArrayNode get() {
        val array = _nodeFactory.arrayNode()
        for(i in min.asInt() .. max.asInt()) {
            array.add(IntNode(i))
        }
        return array
    }

    /**
     * Return `true` if this range containes [value].
     */
    fun contains(value: BigDecimal): Boolean {
        return (min.decimalValue() <= value) && (value <= max.decimalValue())
    }

    /**
     * Return `true` if [other] represents the same range of this object.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as RangeNode

        if (max != other.max) return false
        if (min != other.min) return false

        return true
    }

    /**
     * See [Any.hashCode].
     */
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + max.hashCode()
        result = 31 * result + min.hashCode()
        return result
    }

} //~ RangeNode