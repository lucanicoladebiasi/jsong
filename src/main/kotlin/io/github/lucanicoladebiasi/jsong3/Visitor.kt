package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong3BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong3Parser
import io.github.lucanicoladebiasi.jsong3.functions.NumericFunctions
import org.apache.commons.text.StringEscapeUtils
import java.lang.IllegalArgumentException
import java.math.MathContext

class Visitor(
    private val context: JsonNode?,
    private val loop: Int?,
    private val mapper: ObjectMapper,
    private val mathContext: MathContext,
    private val variables: MutableMap<String, JsonNode>
) : JSong3BaseVisitor<JsonNode?>() {

    companion object {

        private fun compare(lhs: JsonNode, rhs: JsonNode): Int {
            return when {
                lhs.isNumber && rhs.isNumber -> lhs.decimalValue().compareTo(rhs.decimalValue())
                else -> lhs.textValue().compareTo(rhs.textValue())
            }
        }

        private fun expand(mapper: ObjectMapper, node: JsonNode?): ArrayNode {
            val result = mapper.createArrayNode()
            if (node != null) when (node) {
                is ArrayNode -> result.addAll(node)
                else -> result.add(node)
            }
            return result
        }

        private fun filter(mapper: ObjectMapper, predicates: Array<Boolean>, arrayNode: ArrayNode): ArrayNode {
            val result = mapper.createArrayNode()
            if (predicates.size == arrayNode.size()) {
                predicates.forEachIndexed { index, predicate ->
                    if (predicate) {
                        result.add(arrayNode[index])
                    }
                }
            } else TODO("exception not same size!")
            return result
        }

        private fun predicate(node: JsonNode?): Boolean {
            when (node) {
                null -> return false
                is ArrayNode -> {
                    node.forEach { element ->
                        if (predicate(element))
                            return true
                    }
                    return false
                }

                is BooleanNode -> return node.booleanValue()
                is NumericNode -> return node.decimalValue() != DecimalNode.ZERO
                is ObjectNode -> return node.size() > 0
                is TextNode -> return node.textValue().isNotEmpty()
                else -> return false
            }
        }

        private fun predicate(node: NumericNode, index: Int, size: Int): Boolean {
            val v = node.asInt()
            return index == if (v < 0) size + v else v
        }

        private fun predicate(node: RangeNode, index: Int, size: Int): Boolean {
            return node.indexes.map { v -> if (v < 0) size + v else v }.contains(index)
        }

        private fun reduce(node: JsonNode?): JsonNode? {
            return when (node) {
                is ArrayNode -> when (node.size()) {
                    0 -> null
                    1 -> node[0]
                    else -> node
                }

                else -> node
            }
        }

        private fun sanitise(txt: String): String {
            return StringEscapeUtils.unescapeJson(
                when {
                    txt.startsWith("`") && txt.endsWith("`")
                            || txt.startsWith("'") && txt.endsWith("'")
                            || txt.startsWith("\"") && txt.endsWith("\"") -> {
                        txt.substring(1, txt.length - 1)
                    }

                    else -> txt
                }
            )
        }


        private fun stretch(mapper: ObjectMapper, size: Int, node: JsonNode): ArrayNode {
            val result = mapper.createArrayNode()
            val array = expand(mapper, node)
            repeat(size / array.size()) {
                result.addAll(array)
            }
            return result
        }


        private fun stretch(
            mapper: ObjectMapper,
            size: Int,
            variables: Map<String, JsonNode>
        ): MutableMap<String, JsonNode> {
            val map = mutableMapOf<String, JsonNode>()
            variables.forEach { (name, variable) ->
                when (variable) {
                    is BindContextNode -> {
                        // todo check what if size < variable.size or not multiples
                        if (size > variable.size()) {
                            val carry = BindContextNode(mapper)
                            val ratio = size / variable.size()
                            variable.forEach { element ->
                                repeat(ratio) {
                                    carry.add(element)
                                }
                            }
                            map[name] = carry
                        } else map[name] = variable
                    }

                    is BindPositionNode -> {
                        // todo check what if size < variable.size or not multiples
                        if (size > variable.size()) {
                            val carry = BindPositionNode(mapper)
                            val ratio = size / variable.size()
                            variable.forEach { element ->
                                repeat(ratio) {
                                    carry.add(element)
                                }
                            }
                        } else map[name] = variable
                    }

                    else -> map[name] = variable
                }
            }
            return map
        }

    } //~ companion

    override fun visitArray(ctx: JSong3Parser.ArrayContext): JsonNode {
        val result = mapper.createArrayNode()
        ctx.element().forEach { elementCtx ->
            Visitor(context, loop, mapper, mathContext, variables).visit(elementCtx)?.let { element ->
                result.add(element)
            }
        }
        return result
    }

    @Throws(IllegalArgumentException::class)
    override fun visitEvaluateNegate(ctx: JSong3Parser.EvaluateNegateContext): DecimalNode {
        return DecimalNode(
            NumericFunctions.decimalOf(
                reduce(
                    Visitor(context, loop, mapper, mathContext, variables).visit(ctx.exp())
                )
            ).negate()
        )
    }

    override fun visitFalse(ctx: JSong3Parser.FalseContext): BooleanNode {
        return BooleanNode.FALSE
    }

    override fun visitId(ctx: JSong3Parser.IdContext): JsonNode? {
        val fieldName = sanitise(ctx.ID().text)
        return if (context is ObjectNode && context.has(fieldName)) {
            context[fieldName]
        } else
            null
    }

    override fun visitJsong(ctx: JSong3Parser.JsongContext): JsonNode? {
        var context = this.context
        ctx.exp()?.forEach {  expCtx ->
            context = Visitor(context, loop, mapper, mathContext, variables).visit(expCtx)
        }
        return reduce(context)
    }

    override fun visitNull(ctx: JSong3Parser.NullContext?): NullNode {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSong3Parser.NumberContext): DecimalNode {
        return DecimalNode(ctx.NUMBER().text.toBigDecimal())
    }

    override fun visitObject(ctx: JSong3Parser.ObjectContext): ObjectNode {
        val result = mapper.createObjectNode()
        ctx.field().forEachIndexed { index, fieldCtx ->
            val key = reduce(Visitor(context, loop, mapper, mathContext, variables).visit(fieldCtx.key))?.asText()
                ?: index.toString()
            val value = Visitor(context, loop, mapper, mathContext, variables).visit(fieldCtx.`val`)
            result.set<JsonNode>(key, value)
        }
        return result
    }

    override fun visitRange(ctx: JSong3Parser.RangeContext): RangeNode {
        val lhs =
            NumericFunctions.decimalOf(reduce(Visitor(context, loop, mapper, mathContext, variables).visit(ctx.lhs)))
        val rhs =
            NumericFunctions.decimalOf(reduce(Visitor(context, loop, mapper, mathContext, variables).visit(ctx.rhs)))
        return RangeNode.between(lhs, rhs, mapper)
    }

    override fun visitRegex(ctx: JSong3Parser.RegexContext): RegexNode {
        val pattern = ctx.pattern().text
        return when {
            ctx.REG_CI() != null -> RegexNode.ci(pattern)
            ctx.REG_ML() != null -> RegexNode.ml(pattern)
            else -> RegexNode.of(pattern)
        }
    }

    override fun visitText(ctx: JSong3Parser.TextContext): TextNode {
        return TextNode(sanitise(ctx.STRING().text))
    }

    override fun visitTrue(ctx: JSong3Parser.TrueContext?): BooleanNode {
        return BooleanNode.TRUE
    }


}//~ Visitor