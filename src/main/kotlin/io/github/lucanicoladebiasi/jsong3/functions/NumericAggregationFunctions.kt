package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.DecimalNode
import java.math.MathContext

@Suppress("unused")
class NumericAggregationFunctions(private val mc: MathContext) {

    /**
     * https://docs.jsonata.org/aggregation-functions#sum
     */
    @LibraryFunction
    fun sum(array: ArrayNode): DecimalNode {
        return DecimalNode(array.sumOf { it.decimalValue() })
    }

    /**
     * https://docs.jsonata.org/aggregation-functions#max
     */
    @LibraryFunction
    fun max(array: ArrayNode): DecimalNode {
        return DecimalNode(array.maxOf { it.decimalValue() })
    }

    /**
     * https://docs.jsonata.org/aggregation-functions#min
     */
    @LibraryFunction
    fun min(array: ArrayNode): DecimalNode {
        return DecimalNode(array.minOf { it.decimalValue() })
    }

    /**
     * https://docs.jsonata.org/aggregation-functions#average
     */
    @LibraryFunction
    fun average(array: ArrayNode): DecimalNode {
        return DecimalNode(array.sumOf { it.decimalValue() }.divide(array.size().toBigDecimal(), mc))
    }

} //~ NumericAggregationFunctions