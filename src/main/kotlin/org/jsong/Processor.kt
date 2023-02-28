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
package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jsong.antlr.JSongBaseVisitor
import org.jsong.antlr.JSongLexer
import org.jsong.antlr.JSongParser
import java.lang.reflect.InvocationTargetException
import java.math.MathContext
import java.time.Instant
import java.util.*
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.javaType

/**
 *
 *
 *
 * LHS and `lhs` refer to the Left Hand Side of a binary expression.
 * RHS and `rhs` refer to the Right Hand Side of a binary expression.
 */
class Processor(
    private val root: JsonNode? = null,
    private val mc: MathContext = MathContext.DECIMAL128,
    val om: ObjectMapper = ObjectMapper(),
    val random: Random = Random.Default,
    val time: Instant = Instant.now()
) : JSongBaseVisitor<JsonNode?>() {

    companion object {

        private const val BACKTICK = '`'

        private fun normalizeFieldName(tag: String): String {
            return when {
                tag[0] == BACKTICK && tag[tag.length - 1] == BACKTICK -> tag.substring(1, tag.length - 1)
                else -> tag
            }
        }

    } //~ companion

    private var context = root

    private val indexStack = ArrayDeque<Int>()

    private var isToReduce: Boolean = true

    private val lib: JSonataFunctions = Library(this)

    val nf: JsonNodeFactory = om.nodeFactory

    private val ctxMap = mutableMapOf<String, ArrayNode>()

    private val posMap = mutableMapOf<String, ArrayNode>()

    private val varMap = mutableMapOf<String, JsonNode?>()

    private fun descendants(node: JsonNode?): ArrayNode {
        val result = ArrayNode(nf)
        node?.fields()?.forEach { field ->
            if (field.value != null) {
                result.addAll(descendants(field.value))
                result.add(field.value)
            }
        }
        return result
    }

    fun evaluate(exp: String): JsonNode? {
        val canon = exp.replace("\\s".toRegex() , " ")  // todo: ANTLR doesn't skip spaces
        return visit(JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(canon)))).jsong())
    }

    internal fun expand(node: JsonNode?): ArrayNode {
        return when (node) {
            null -> ArrayNode(nf)
            is RangeNode -> RangesNode(nf).add(node)
            is ArrayNode -> node
            else -> ArrayNode(nf).add(node)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun isCallable(
        kFunction: KFunction<*>,
        args: List<Any?>
    ): Boolean {
        var isCallable = true
        for (index in 1 until kFunction.parameters.size) {
            val kParameter = kFunction.parameters[index]
            when (kParameter.isOptional) {
                true -> isCallable = isCallable && true
                else -> when (index < args.size) {
                    true -> {
                        val arg = args[index]
                        when (arg == null) {
                            true -> isCallable = isCallable && kParameter.type.isMarkedNullable
                            else -> isCallable = isCallable
                                    && (kParameter.type.javaType as Class<*>).isAssignableFrom(arg::class.java)
                        }
                    }

                    else -> return false
                }
            }
        }
        return isCallable
    }

    private fun recall(
        kClass: KClass<*>,
        functionName: String,
        args: List<Any?>
    ): KFunction<*> {
        kClass.memberFunctions
            .filter { it.name == functionName }
            .sortedByDescending { it.parameters.size }
            .forEach { kFunction ->
                if (isCallable(kFunction, args)) {
                    return kFunction
                }
            }
        throw IllegalArgumentException(
            "Function $functionName(${args.subList(1, args.size).joinToString(", ")}) not found"
        )
    }


    private fun reduce(node: JsonNode?): JsonNode? {
        return if (isToReduce) when (node) {
            is ArrayNode -> when (node.size()) {
                0 -> null

                1 -> node[0]
                else -> {
                    val result = nf.arrayNode()
                    node.forEach { element ->
                        reduce(element)?.let { result.add(it) }
                    }
                    result
                }
            }

            else -> node
        } else node
    }

    private fun select(node: JsonNode?, fieldName: String): JsonNode? {
        return when (node) {
            is ObjectNode -> {
                val field = normalizeFieldName(fieldName)
                when (this.context?.has(field)) {
                    true -> this.context?.get(field)
                    else -> null
                }
            }

            else -> null
        }
    }

    private fun shrink(array: ArrayNode, predicates: BooleanArray): ArrayNode {
        val value = nf.arrayNode()
        predicates.forEachIndexed { index, predicate ->
            if (predicate) {
                value.add(array[index])
            }
        }
        return value
    }

    private fun stretch(array: ArrayNode, size: Int): ArrayNode {
        val value = nf.arrayNode()
        val ratio = size / array.size()
        array.forEach { element ->
            for (i in 0 until ratio) {
                value.add(element)
            }
        }
        return value
    }

    override fun visitAdd(ctx: JSongParser.AddContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().add(rhs.decimalValue()))
    }

    override fun visitAll(ctx: JSongParser.AllContext): JsonNode? {
        val result = ArrayNode(nf)
        if (context is ObjectNode) {
            context?.fields()?.forEach { field ->
                result.add(field.value)
            }
        }
        return reduce(result)
    }

    override fun visitAnd(ctx: JSongParser.AndContext): JsonNode? {
        val lhs = lib.boolean(visit(ctx.lhs))
        val rhs = lib.boolean(visit(ctx.rhs))
        return BooleanNode.valueOf(lhs.booleanValue() && rhs.booleanValue())
    }

    override fun visitArray(ctx: JSongParser.ArrayContext): JsonNode {
        val result = ArrayNode(nf)
        ctx.exp().forEach { exp ->
            result.add(visit(exp))
        }
        return result
    }

    override fun visitBool(ctx: JSongParser.BoolContext): JsonNode? {
        return when {
            ctx.FALSE() != null -> BooleanNode.FALSE
            ctx.TRUE() != null -> BooleanNode.TRUE
            else -> throw IllegalArgumentException("$ctx not recognized")
        }

    }

    override fun visitCall(ctx: JSongParser.CallContext): JsonNode? {
        return when (val function = varMap[ctx.label().text]) {

            is FunNode -> {
                val context = this.context
                ctx.exp().forEachIndexed { i, exp ->
                    this.context = context
                    varMap[function.args[i]] = visit(exp)
                    this.context = varMap[function.args[i]]
                }
                return visit(JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(function.body)))).jsong())
            }

            else -> {
                val args = mutableListOf<Any?>()
                val context = this.context
                ctx.exp().forEach { exp ->
                    this.context = context
                    args.add(visit(exp))
                }
                var kfun: KFunction<*>
                args.add(0, lib)
                try {
                    kfun = recall(lib::class, ctx.label().text, args)
                } catch (e: IllegalArgumentException) {
                    args.add(1, context)
                    kfun = recall(lib::class, ctx.label().text, args)
                }
                while (args.size < kfun.parameters.size) {
                    args.add(null)
                }
                try {
                    kfun.call(*args.toTypedArray()) as JsonNode?
                } catch (e: InvocationTargetException) {
                    throw e.targetException
                }
            }
        }
    }

    override fun visitChain(ctx: JSongParser.ChainContext): JsonNode? {
        context = visit(ctx.lhs)
        return visit(ctx.rhs)
    }

    override fun visitConcatenate(ctx: JSongParser.ConcatenateContext): JsonNode {
        val sb = StringBuilder()
        visit(ctx.lhs)?.let { lhs ->
            sb.append(lib.string(lhs).textValue())
        }
        visit(ctx.rhs)?.let { rhs ->
            sb.append(lib.string(rhs).textValue())
        }
        return TextNode(sb.toString())
    }

    override fun visitCondition(ctx: JSongParser.ConditionContext): JsonNode? {
        return when (lib.boolean(visit(ctx.prd)).booleanValue()) {
            true -> visit(ctx.pos)
            else -> visit(ctx.neg)
        }
    }

    override fun visitContext(ctx: JSongParser.ContextContext): JsonNode? {
        return context
    }

    override fun visitDefine(ctx: JSongParser.DefineContext): JsonNode? {
        when (ctx.exp().text.contains("~>")) {
            true -> varMap[ctx.label().text] = FunNode(listOf("context"), ctx.exp().text, nf)
            else -> varMap[ctx.label().text] = visit(ctx.exp())
        }
        return varMap[ctx.label().text]
    }

    override fun visitDescendants(ctx: JSongParser.DescendantsContext): JsonNode? {
        val result = ArrayNode(nf)
        if (context is ObjectNode) {
            result.addAll(descendants(context))
        }
        return reduce(result)
    }

    override fun visitDiv(ctx: JSongParser.DivContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().divide(rhs.decimalValue(), mc))
    }

    override fun visitEq(ctx: JSongParser.EqContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        return BooleanNode.valueOf(lhs == rhs)
    }

    override fun visitExpand(ctx: JSongParser.ExpandContext): JsonNode {
        val result = expand(visit(ctx.exp()))
        isToReduce = false
        return result
    }

    override fun visitField(ctx: JSongParser.FieldContext): JsonNode? {
        return select(context, ctx.text)
    }

    override fun visitFilter(ctx: JSongParser.FilterContext): JsonNode? {
        val result = ArrayNode(nf)
        val lhs = expand(visit(ctx.lhs))
        val prd = BooleanArray(lhs.size())
        lhs.forEachIndexed { index, context ->
            this.context = context
            indexStack.push(index)
            val rhs = visit(ctx.rhs)
            when (rhs) {
                is NumericNode -> {
                    val value = rhs.asInt()
                    val offset = if (value < 0) lhs.size() + value else value
                    prd[index] = index == offset
                    if (prd[index]) {
                        result.add(context)
                    }
                }

                is RangesNode -> {
                    prd[index] = rhs.indexes.map { it.asInt() }.contains(index)
                    if (prd[index]) {
                        result.add(context)
                    }
                }

                else -> {
                    prd[index] = lib.boolean(rhs).asBoolean()
                    if (prd[index]) {
                        result.add(context)
                    }
                }

            }
            indexStack.pop()
            this.context = null
        }
        ctxMap.forEach { label, array ->
            ctxMap[label] = shrink(array, prd)
        }
        posMap.forEach { label, array ->
            posMap[label] = shrink(array, prd)
        }
        return reduce(result)
    }

    override fun visitFun(ctx: JSongParser.FunContext): FunNode {
        return FunNode(
            ctx.label().map { label -> label.text },
            ctx.exp().text
        )
    }

    override fun visitFunction(ctx: JSongParser.FunctionContext): JsonNode {
        return visit(ctx.`fun`())!!
    }

    override fun visitGt(ctx: JSongParser.GtContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val result = BooleanNode.valueOf(
            when {
                lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() > rhs.decimalValue()
                else -> lib.string(lhs).textValue() > lib.string(rhs).textValue()
            }
        )
        return result
    }

    override fun visitGte(ctx: JSongParser.GteContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val result = BooleanNode.valueOf(
            when {
                lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() >= rhs.decimalValue()
                else -> lib.string(lhs).textValue() >= lib.string(rhs).textValue()
            }
        )
        return result
    }

    override fun visitIn(ctx: JSongParser.InContext): JsonNode? {
        val lhs = reduce(visit(ctx.lhs))
        val rhs = expand(visit(ctx.rhs))
        return BooleanNode.valueOf(rhs.contains(lhs))
    }

    override fun visitJsong(ctx: JSongParser.JsongContext): JsonNode? {
        var result: JsonNode? = null
        ctx.exp().forEach { exp ->
            result = visit(exp)
        }
        return result
    }

    override fun visitLambda(ctx: JSongParser.LambdaContext): JsonNode? {
        val function = visitFun(ctx.`fun`())
        val context = this.context
        ctx.exp().forEachIndexed { i, exp ->
            this.context = context
            varMap[function.args[i]] = visit(exp)
        }
        return visit(JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(function.body)))).jsong())
    }

    // todo: functions should have their map or have a single map and discrimite returns.
    override fun visitLbl(ctx: JSongParser.LblContext): JsonNode? {
        val label = ctx.label().text
        val result = when (val positionalValue = posMap[label]) {
            null -> when (val contextualValue: ArrayNode? = ctxMap[label]) {
                null -> {
                    when(varMap[label]) {
                        null -> {
                            val args = mutableListOf<Any?>()
                            args.add(lib)
                            args.add(context)
                            try {
                                recall(lib::class, label, args).call(*args.toTypedArray()) as JsonNode?
                            } catch (e: IllegalArgumentException) {
                                null
                            }
                        }
                        else -> varMap[label]
                    }
                }
                else -> when (indexStack.isEmpty()) {
                    true -> contextualValue
                    else -> contextualValue[indexStack.peek()]
                }
            }

            else -> IntNode(positionalValue.indexOf(context) + 1)
        }
        return when (result) {
            is FunNode -> {
                visit(JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(result.body)))).jsong())
            }
            else -> result
        }
    }

    override fun visitLt(ctx: JSongParser.LtContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val result = BooleanNode.valueOf(
            when {
                lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() < rhs.decimalValue()
                else -> lib.string(lhs).textValue() < lib.string(rhs).textValue()
            }
        )
        return result
    }

    override fun visitLte(ctx: JSongParser.LteContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        val result = BooleanNode.valueOf(
            when {
                lhs is NumericNode && rhs is NumericNode -> lhs.decimalValue() <= rhs.decimalValue()
                else -> lib.string(lhs).textValue() <= lib.string(rhs).textValue()
            }
        )
        return result
    }

    override fun visitMap(ctx: JSongParser.MapContext): JsonNode? {
        val result = ArrayNode(nf)
        val lhs = expand(visit(ctx.lhs))
        when (lhs) {
            is RangesNode -> lhs.indexes.forEach { context ->
                this.context = context
                when (val rhs = visit(ctx.rhs)) {
                    is ArrayNode -> result.addAll(rhs)
                    else -> rhs?.let { result.add(it) }
                }
            }

            else -> lhs.forEachIndexed { index, context ->
                this.context = context
                indexStack.push(index)
                when (val rhs = visit(ctx.rhs)) {
                    is ArrayNode -> result.addAll(rhs)
                    else -> rhs?.let { result.add(it) }
                }
                indexStack.pop()
            }
        }
        return reduce(result)
    }

    override fun visitMapctx(ctx: JSongParser.MapctxContext): JsonNode? {
        var result = ArrayNode(nf)
        val lhs = expand(visit(ctx.lhs))
        when (lhs) {
            is RangesNode -> lhs.indexes.forEach { context ->
                this.context = context
                when (val rhs = visit(ctx.rhs)) {
                    is ArrayNode -> result.addAll(rhs)
                    else -> rhs?.let { result.add(it) }
                }
            }

            else -> lhs.forEachIndexed { index, context ->
                this.context = context
                indexStack.push(index)
                when (val rhs = visit(ctx.rhs)) {
                    is ArrayNode -> result.addAll(rhs)
                    else -> rhs?.let { result.add(it) }
                }
                indexStack.pop()
            }
        }
        ctxMap.forEach { label, array ->
            ctxMap[label] = stretch(array, result.size())
        }
        posMap.forEach { label, array ->
            posMap[label] = stretch(array, result.size())
        }
        val ratio = result.size() / lhs.size()
        ctxMap[ctx.label().text] = result
        result = ArrayNode(nf)
        for (i in 0 until ratio) {
            result.addAll(lhs)
        }
        return reduce(result)
    }

    override fun visitMappos(ctx: JSongParser.MapposContext): JsonNode? {
        val result = ArrayNode(nf)
        val lhs = expand(visit(ctx.lhs))
        when (lhs) {
            is RangesNode -> lhs.indexes.forEach { context ->
                this.context = context
                when (val rhs = visit(ctx.rhs)) {
                    is ArrayNode -> result.addAll(rhs)
                    else -> rhs?.let { result.add(it) }
                }
            }

            else -> lhs.forEachIndexed { index, context ->
                this.context = context
                indexStack.push(index)
                when (val rhs = visit(ctx.rhs)) {
                    is ArrayNode -> result.addAll(rhs)
                    else -> rhs?.let { result.add(it) }
                }
                indexStack.pop()
            }
        }
        ctxMap.forEach { label, array ->
            ctxMap[label] = stretch(array, result.size())
        }
        posMap.forEach { label, array ->
            posMap[label] = stretch(array, result.size())
        }
        posMap[ctx.label().text] = result
        return reduce(result)
    }

    override fun visitMod(ctx: JSongParser.ModContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().remainder(rhs.decimalValue()))
    }

    override fun visitMul(ctx: JSongParser.MulContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().multiply(rhs.decimalValue()))
    }

    override fun visitNe(ctx: JSongParser.NeContext): JsonNode? {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        return BooleanNode.valueOf(lhs != rhs)
    }

    override fun visitNihil(ctx: JSongParser.NihilContext): JsonNode? {
        return NullNode.instance
    }

    override fun visitNumber(ctx: JSongParser.NumberContext): JsonNode {
        return DecimalNode(ctx.text.toBigDecimal())
    }

    override fun visitObj(ctx: JSongParser.ObjContext): JsonNode {
        val result = ObjectNode(nf)
        ctx.pair().forEachIndexed { index, pair ->
            val key = visit(pair.key)?.asText() ?: index.toString()
            val value = visit(pair.value) ?: NullNode.instance
            result.set<JsonNode>(key, value)
        }
        return result
    }

    /**
     * Return the [BooleanNode] from the [ctx] content defined as
     *
     * `| lhs = exp OR   rhs = exp                      #or`
     *
     * LHS an RHS are converted to booleans calling [JSonataFunctions.boolean].
     *
     */
    override fun visitOr(
        ctx: JSongParser.OrContext
    ): BooleanNode {
        val lhs = lib.boolean(visit(ctx.lhs))
        val rhs = lib.boolean(visit(ctx.rhs))
        return BooleanNode.valueOf(lhs.booleanValue() || rhs.booleanValue())
    }

    /**
     * Return the [RangeNode] from the [ctx] content defined as
     *
     * ```
     * range
     *     : min = exp '..' max = exp
     *     ;
     * ```
     *
     * The `min` and `max` limits are converted to numbers calling [JSonataFunctions.number].
     *
     * @see visitRanges
     */
    override fun visitRange(
        ctx: JSongParser.RangeContext
    ): RangeNode {
        val min = lib.number(visit(ctx.min))
        val max = lib.number(visit(ctx.max))
        return RangeNode.of(min.decimalValue(), max.decimalValue(), nf)
    }

    /**
     * Return the [RangesNode] from the [ctx] content defined as
     *
     * `| '[' range (',' range)* ']'                        #ranges`
     *
     * @see visitRange
     */
    override fun visitRanges(
        ctx: JSongParser.RangesContext
    ): RangesNode {
        val result = RangesNode(nf)
        ctx.range().forEach { range ->
            result.add(visit(range))
        }
        return result
    }

    /**
     * Return the [RegexNode] from the [ctx] content defined as
     *
     * `| REGEX                                         #regex`
     *
     */
    override fun visitRegex(
        ctx: JSongParser.RegexContext
    ): RegexNode {
        return RegexNode(ctx.REGEX().text)
    }

    /**
     * Return the root of the evaluated expression.
     *
     * @return [JsonNode] can be `null`.
     *
     * @see evaluate
     */
    override fun visitRoot(
        ctx: JSongParser.RootContext
    ): JsonNode? {
        return root
    }

    /**
     * Return the last context from the [ctx] content defined as
     *
     * `| '(' exp (';' exp)* ')'`
     *
     * @return [JsonNode] can be `null`.
     *
     */
    override fun visitScope(
        ctx: JSongParser.ScopeContext
    ): JsonNode? {
        val context = this.context
        ctx.exp().forEach { exp ->
            this.context = context
            this.context = visit(exp)
        }
        return this.context
    }

    /**
     * Return the [DecimalNode] from [ctx] context defined as
     *
     * `| lhs = exp '-' rhs = exp                       #sub`
     *
     * LHS and RHS are cast to numbers.
     *
     * @see [JSonataFunctions.number]
     */
    override fun visitSub(
        ctx: JSongParser.SubContext
    ): DecimalNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().subtract(rhs.decimalValue()))
    }

    /**
     * Return the [TextNode] from the [ctx] content defined as
     *
     * ```
     * txt
     *     : STRING
     *     ;
     * ```
     */
    override fun visitTxt(
        ctx: JSongParser.TxtContext
    ): TextNode {
        return TextNode(ctx.text.substring(1, ctx.text.length - 1))
    }

} //~ Processor

