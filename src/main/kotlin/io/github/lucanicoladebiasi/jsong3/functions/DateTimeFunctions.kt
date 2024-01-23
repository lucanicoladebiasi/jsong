package io.github.lucanicoladebiasi.jsong3.functions

import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode

class DateTimeFunctions {

    /**
     * https://docs.jsonata.org/date-time-functions#now
     */
    @LibraryFunction
    fun now(): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/date-time-functions#now
     */
    @LibraryFunction
    fun now(picture: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/date-time-functions#now
     */
    @LibraryFunction
    fun now(picture: TextNode, timezone: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/date-time-functions#millis
     */
    @LibraryFunction
    fun millis(): DecimalNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/date-time-functions#frommillis
     */
    @LibraryFunction
    fun fromMillis(number: NumericNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/date-time-functions#frommillis
     */
    @LibraryFunction
    fun fromMillis(number: NumericNode, picture: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/date-time-functions#frommillis
     */
    @LibraryFunction
    fun fromMillis(number: NumericNode, picture: TextNode, timezone: TextNode): TextNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/date-time-functions#tomillis
     */
    @LibraryFunction
    fun toMillis(timestamp: TextNode): DecimalNode {
        TODO()
    }

    /**
     * https://docs.jsonata.org/date-time-functions#tomillis
     */
    @LibraryFunction
    fun toMillis(timestamp: TextNode, picture: TextNode): DecimalNode {
        TODO()
    }

} //~ DateTimeFunctions