package io.github.lucanicoladebiasi.jsong2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import io.github.lucanicoladebiasi.jsong.antlr.JSong2BaseVisitor
import io.github.lucanicoladebiasi.jsong.antlr.JSong2Parser
import org.apache.commons.text.StringEscapeUtils

class Processor(
    private val root: JsonNode?,
    private val mapr: ObjectMapper
) : JSong2BaseVisitor<Sequence>() {

    companion object {

        fun sanitise(
            txt: String
        ): String {
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


    } //~ companion

    private val nf = mapr.nodeFactory

    private val operands = Stack<Sequence>()

    private val path = Stack<Sequence>()

    init {
        operands.push(path.push(Sequence(nf).append(root)))
    }

    private fun descendants(
        node: JsonNode
    ): Sequence {
        val result = Sequence(nf)
        when (node) {
            is ArrayNode -> {
                node.forEach {
                    result.addAll(descendants(it))
                }
            }

            is ObjectNode -> {
                result.add(node)
                node.fields().forEach { field ->
                    if (field != null) {
                        result.addAll(descendants(field.value))
                    }
                }
            }

            else -> {
                result.add(node)
            }
        }
        return result
    }

    override fun visitArray(
        ctx: JSong2Parser.ArrayContext
    ): Sequence {
        val array = nf.arrayNode()
        val context = operands.pop() ?: Sequence(nf)
        ctx.exp().forEach { exp ->
            operands.push(context)
            visit(exp)
            array.add(operands.pop()?.value)
        }
        return operands.push(Sequence(nf).append(array))
    }

    override fun visitBoolean(
        ctx: JSong2Parser.BooleanContext
    ): Sequence {
        return operands.push(Sequence(nf).append(BooleanNode.valueOf(ctx.TRUE() != null)))
    }

    override fun visitDescendants(
        ctx: JSong2Parser.DescendantsContext
    ): Sequence {
        val result = Sequence(nf)
        val context = operands.pop()?.value
        if (context != null) {
            result.append(descendants(context))
        }
        return operands.push(result)
    }

    override fun visitExp_to_eof(
        ctx: JSong2Parser.Exp_to_eofContext
    ): Sequence {
        super.visitExp_to_eof(ctx)
        return operands.pop() ?: Sequence(nf)
    }

    override fun visitFields(
        ctx: JSong2Parser.FieldsContext
    ): Sequence {
        val result = Sequence(nf)
        val context = operands.pop()?.value
        if (context is ObjectNode) {
            context.fields().forEach { field ->
                if (field.value != null) {
                    result.append(field.value)
                }
            }
        }
        return operands.push(result)
    }

    override fun visitFilter(
        ctx: JSong2Parser.FilterContext
    ): Sequence {
        visit(ctx.lhs)
        visit(ctx.rhs)
        val predicate = operands.pop()?.value
        val pop = operands.pop() ?: Sequence(nf)
        val result = Sequence(nf)
        if (predicate != null) {
            when {
                predicate.isNumber -> {
                    pop.forEach { context ->
                        val size = context.size()
                        val offset = predicate.asInt()
                        val index = if (offset < 0) size + offset else offset
                        if (index in 0 until size) {
                            result.append(context[index])
                        }
                    }
                }

                else -> TODO()
            }
        }
        return operands.push(result)
    }

    override fun visitMap(
        ctx: JSong2Parser.MapContext
    ): Sequence {
        val result = Sequence(nf)
        operands.pop()?.flatten?.forEachIndexed { index, context ->
            operands.push(Sequence(nf).append(context))
            visit(ctx.exp())
            result.append(operands.pop())
        }
        return operands.push(path.push(result))
    }

    override fun visitNegative(
        ctx: JSong2Parser.NegativeContext
    ): Sequence {
        visit(ctx.exp())
        val context = operands.pop()
        val number =
            context?.value?.decimalValue()?.negate() ?: throw NotNumericException(ctx, "${ctx.text} not a number")
        return operands.push(Sequence(nf).append(DecimalNode(number)))
    }

    override fun visitNull(
        ctx: JSong2Parser.NullContext
    ): Sequence {
        return operands.push(Sequence(nf).append(NullNode.instance))
    }

    override fun visitNumber(
        ctx: JSong2Parser.NumberContext
    ): Sequence {
        return operands.push(Sequence(nf).append(DecimalNode(ctx.NUMBER().text.toBigDecimal())))
    }

    override fun visitObject(
        ctx: JSong2Parser.ObjectContext
    ): Sequence {
        val obj = ObjectNode(nf)
        //cursors.push(0)
        //val mark = path.size
        val context = operands.pop() ?: Sequence(nf)
        ctx.field().forEachIndexed { index, field ->
            //cursors.poke(index)
            //println("obj: ${cursors.size}, ${cursors.peek()}")
            operands.push(context)
            visit(field.key)
            val key = operands.pop()?.value?.asText() ?: index.toString()
            operands.push(context)
            field.value.forEach {
                visit(it)
            }
            val value = operands.pop()?.value ?: NullNode.instance
            obj.set<JsonNode>(key, value)
//            while (cursors.size > mark) {
//                cursors.pop()
//            }
//            while (path.size > mark) {
//                path.pop()
//            }
        }
        //cursors.pop()
        return operands.push(Sequence(nf).append(obj))
    }

    override fun visitParent(
        ctx: JSong2Parser.ParentContext
    ): Sequence {
        val result =  Sequence(nf)
        val context = operands.pop()?.flatten ?: Sequence(nf)
        val parent = path[path.size - ctx.MODULE().size - 1].flatten
        val ratio = parent.size().toFloat() / context.size().toFloat()
        repeat(context.size()) {index ->
            result.append(parent[(ratio * index).toInt()])
        }
        return operands.push(result)
    }

    override fun visitSelect(
        ctx: JSong2Parser.SelectContext
    ): Sequence {
        val result = Sequence(nf)
        val id = sanitise(ctx.ID().text)
        operands.pop()?.forEach { context ->
            context.filterIsInstance<ObjectNode>().filter { node ->
                node.has(id)
            }.forEach { node ->
                result.append(node[id])
            }
        }
        return operands.push(result)
    }

    override fun visitString(
        ctx: JSong2Parser.StringContext
    ): Sequence {
        return operands.push(Sequence(nf).append(TextNode(sanitise(ctx.text))))
    }

} //~ Processor