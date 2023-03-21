package io.github.lucanicoladebiasi.jsong

import com.fasterxml.jackson.databind.JsonNode

/**
 * This class is thrown with [message] when the Full Qualified Name of a function is not found.
 *
 * @see Processor.visitCall
 */
class FunctionNotFoundException(
    message: String
): NoSuchMethodException(message)

/**
 * This class is thrown with [message] when [FunctionNode] is expected in vain.
 *
 * @param message shows the body of the node and its class.
 */
class FunctionTypeException(
    message: String
): ClassCastException(message) {

    companion object {

        /**
         * Create a new [FunctionTypeException] because [node] is not [FunctionNode].
         */
        fun forNode(node: JsonNode): FunctionTypeException {
            return FunctionTypeException("node ${node.asText()}: ${node::class} is not a function")
        }

    } //~ companion

} //~ FunctionTypeException


