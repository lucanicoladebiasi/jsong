package io.github.lucanicoladebiasi.jsong2

import org.antlr.v4.runtime.RuleContext

open class JSongException(
    val ctx: RuleContext,
    message: String
): RuntimeException(message)

class NotNumericException(
    ctx: RuleContext,
    message: String
): JSongException(ctx, message)

class OutOfBoundsException(
    ctx: RuleContext,
    message: String
): JSongException(ctx, message)

