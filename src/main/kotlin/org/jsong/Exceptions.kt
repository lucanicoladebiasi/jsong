package org.jsong

import com.fasterxml.jackson.databind.JsonNode

/**
 * This class is thrown when the Full Qualified Name [fqn] of a function is not found.
 *
 * @see Processor.visitCall
 */
class FunctionNotFoundException(
    message: String
): NoSuchMethodException(message) {

    companion object {

        fun forName(name: String): FunctionNotFoundException {
            return FunctionNotFoundException("function `$name` not found")
        }

    } //~ companion

} //~ FunctionNotFoundException

class FunctionTypeException(
    message: String
): ClassCastException(message) {

    companion object {

        fun forNode(node: JsonNode): FunctionTypeException {
            return FunctionTypeException("node ${node.asText()}: ${node::class} is not a function")
        }

    } //~ companion

} //~ FunctionTypeException


