package org.jsong

/**
 * This class is thrown when the Full Qualified Name [fqn] of a function is not found.
 *
 * @see Processor.visitCall
 */
class FunctionNotFoundException(
    val fqn: String,
    override val cause: Throwable? = null
): TypeNotPresentException(fqn, cause) {

    override val message: String
        get() = "$fqn not found"

}