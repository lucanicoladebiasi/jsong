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
import java.math.MathContext
import java.time.Instant
import java.util.*
import kotlin.random.Random
import kotlin.reflect.full.memberFunctions

/**
 *
 * LHS and `lhs` refer to the Left Hand Side of a binary expression.
 * RHS and `rhs` refer to the Right Hand Side of a binary expression.
 *
 * The methods prefixed with `visit` override the ANTLR visiting pattern:
 * the documentation shows the matched pattern from [JSong.g4]() rules.
 * If the matched pattern begins with the `|`, the pattern is a fragment of a rule with multiple alternatives.
 */
class Processor(
    val root: JsonNode? = null,
    val varMap: MutableMap<String, JsonNode?> = mutableMapOf(),
    val mathContext: MathContext = MathContext.DECIMAL128,
    val objectMapper: ObjectMapper = ObjectMapper(),
    val random: Random = Random.Default,
    val time: Instant = Instant.now(),
    val lib: JSONataFunctionLibrary = Library(mathContext, objectMapper, random, time)
) : JSongBaseVisitor<JsonNode?>() {

    companion object {

        /**
         * Character used to wrap a field name escaping JSONata lexer rules.
         *
         * @see normalizeFieldName
         */
        private const val BACKTICK = '`'

        /**
         * Return the field name from [tag] removing the first and the last characters of both are [BACKTICK].
         */
        private fun normalizeFieldName(
            tag: String
        ): String {
            return when {
                tag[0] == BACKTICK && tag[tag.length - 1] == BACKTICK -> tag.substring(1, tag.length - 1)
                else -> tag
            }
        }

    } //~ companion

    private var context = root

    private val indexStack = ArrayDeque<Int>()

    private var isToReduce: Boolean = true

    private val ctxMap = mutableMapOf<String, ArrayNode>()

    private val posMap = mutableMapOf<String, ArrayNode>()

    /**
     * Return an arguments list evaluating a list of JSONata expressions,
     * the arguments list is used to call a function.
     *
     * @param expList list of JSONata expressions.
     *
     * @return [MutableList] of [JsonNode] elements,
     *         is mutable to allow to inject default context,
     *         elements can be `null`.
     *
     * @see call
     * @see visitCall
     * @see visitLambda
     */
    private fun args(
        expList: List<JSongParser.ExpContext>
    ): MutableList<JsonNode?> {
        val argList = mutableListOf<JsonNode?>()
        val context = this.context  // Save the context, following evaluation could change it.
        expList.forEach { exp ->
            this.context = context  // Set the context to the saved value.
            argList.add(visit(exp))
        }
        this.context = context      // Set the context to the saved value.
        return argList
    }

    /**
     * Return the result of calling the [func] function with the [args] parameters.
     *
     * @return [JsonNode] can be `null`.
     *
     * @see args
     * @see visitCall
     * @see visitLambda
     */
    private fun call(
        func: FunctionNode,
        args: List<JsonNode?>
    ): JsonNode? {
        val varMap = mutableMapOf<String, JsonNode?>()
        varMap.putAll(this.varMap)
        args.forEachIndexed { index, node ->
            varMap[func.args[index]] = node
        }
        return Processor(context, varMap, mathContext, objectMapper, random, time, lib).evaluate(func.body)
    }

    /**
     * Return the result if calling the [name] method of the [lib] interface with the [args] parameters.
     *
     * @return [JsonNode] can be `null`.
     *
     * @throws FunctionNotFoundException if [name] ( [args] ) method is not found in [lib].
     *
     * @see args
     * @see visitCall
     */
    @Throws(
        FunctionNotFoundException::class
    )
    private fun call(
        lib: JSONataFunctionLibrary,
        name: String,
        args: MutableList<JsonNode?>
    ): JsonNode? {
        if (args.isEmpty()) {
          args.add(context)
        }
        try {
            val method = lib::class.memberFunctions.first { name == it.name }
            while(args.size < method.parameters.size -1) {
                args.add(null)  // Fill missing args with `null`.
            }
            val result = when(method.parameters.last().isVararg) {
                true -> method.call(lib, args.toTypedArray())
                else -> method.call(lib, *args.toTypedArray())
            }
            return when(result) {
                null -> null
                is JsonNode -> result
                else -> objectMapper.valueToTree(result)
            }
        } catch (e: IllegalArgumentException) {
            throw FunctionNotFoundException(e.message!!)
        } catch (e: NullPointerException) {
            throw FunctionNotFoundException(e.message!!)
        }
    }

    private fun descendants(node: JsonNode?): ArrayNode {
        val result = ArrayNode(objectMapper.nodeFactory)
        node?.fields()?.forEach { field ->
            if (field.value != null) {
                result.addAll(descendants(field.value))
                result.add(field.value)
            }
        }
        return result
    }

    fun evaluate(exp: String): JsonNode? {
        val canon = exp.replace("\\s".toRegex(), " ")  // todo: ANTLR doesn't skip spaces
        return visit(JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(canon)))).jsong())
    }

    /**
     * Return an [ArrayNode]
     * * empty if [node] is null,
     * * being a [RangesNode] instance if [node] is a [RangesNode] instance,
     * * having the [node] element if [node] isn't an array,
     * * the [node] itself if it is a not empty array.
     */
    private fun expand(
        node: JsonNode?
    ): ArrayNode {
        return when (node) {
            null -> objectMapper.nodeFactory.arrayNode()
            is RangeNode -> RangesNode(objectMapper.nodeFactory).add(node)
            is ArrayNode -> node
            else -> objectMapper.nodeFactory.arrayNode().add(node)
        }
    }


//    @OptIn(ExperimentalStdlibApi::class)
//    private fun isCallable(
//        kFunction: KFunction<*>,
//        args: List<Any?>
//    ): Boolean {
//        var isCallable = true
//        for (index in 1 until kFunction.parameters.size) {
//            val kParameter = kFunction.parameters[index]
//            when (kParameter.isOptional) {
//                true -> isCallable = isCallable && true
//                else -> when (index < args.size) {
//                    true -> {
//                        val arg = args[index]
//                        when (arg == null) {
//                            true -> isCallable = isCallable && kParameter.type.isMarkedNullable
//                            else -> isCallable = isCallable
//                                    && (kParameter.type.javaType as Class<*>).isAssignableFrom(arg::class.java)
//                        }
//                    }
//
//                    else -> return false
//                }
//            }
//        }
//        return isCallable
//    }

    private fun reduce(node: JsonNode?): JsonNode? {
        return if (isToReduce) when (node) {
            is ArrayNode -> when (node.size()) {
                0 -> null

                1 -> node[0]
                else -> {
                    val result = objectMapper.nodeFactory.arrayNode()
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
        val value = objectMapper.nodeFactory.arrayNode()
        predicates.forEachIndexed { index, predicate ->
            if (predicate) {
                value.add(array[index])
            }
        }
        return value
    }

    private fun stretch(array: ArrayNode, size: Int): ArrayNode {
        val value = objectMapper.nodeFactory.arrayNode()
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
        val result = ArrayNode(objectMapper.nodeFactory)
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

    override fun visitApp(ctx: JSongParser.AppContext): JsonNode {
        val sb = StringBuilder()
        visit(ctx.lhs)?.let { lhs ->
            sb.append(lib.string(lhs).textValue())
        }
        visit(ctx.rhs)?.let { rhs ->
            sb.append(lib.string(rhs).textValue())
        }
        return TextNode(sb.toString())
    }

    override fun visitArr(ctx: JSongParser.ArrContext): JsonNode {
        val result = ArrayNode(objectMapper.nodeFactory)
        ctx.exp().forEach { exp ->
            result.add(visit(exp))
        }
        return result
    }

    override fun visitBoo(ctx: JSongParser.BooContext): JsonNode? {
        return when {
            ctx.FALSE() != null -> BooleanNode.FALSE
            ctx.TRUE() != null -> BooleanNode.TRUE
            else -> throw IllegalArgumentException("$ctx not recognized")
        }

    }

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| '$' lbl '(' (exp (',' exp)*)? ')'             #call`
     *
     * where `lbl` is the name of the function to call.
     *
     * @return the [JsonNode] can be `null`.
     *
     * @throws FunctionNotFoundException if no function has the `lbl` name.
     * @throws FunctionTypeException if the `lbl` resolves in a node that is not a function.
     *
     * @see args
     * @see call
     */
    @Throws(
        FunctionNotFoundException::class,
        FunctionTypeException::class
    )
    override fun visitCall(
        ctx: JSongParser.CallContext
    ): JsonNode? {
        val fqn = ctx.lbl().text
        val args = args(ctx.exp())
        return when (val func = varMap[fqn]) {
            null -> call(lib, fqn, args)
            else -> when (func) {
                is FunctionNode -> call(func, args)
                else -> throw FunctionTypeException.forNode(func)
            }
        }
    }

//    override fun visitCall(ctx: JSongParser.CallContext): JsonNode? {
//        return when (val function = varMap[ctx.lbl().text]) {
//
//            is FunctionNode -> {
//                val context = this.context
//                ctx.exp().forEachIndexed { i, exp ->
//                    this.context = context
//                    varMap[function.args[i]] = visit(exp)
//                    this.context = varMap[function.args[i]]
//                }
//                return visit(JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(function.body)))).jsong())
//            }
//
//            else -> {
//                val args = mutableListOf<Any?>()
//                val context = this.context
//                ctx.exp().forEach { exp ->
//                    this.context = context
//                    args.add(visit(exp))
//                }
//                var kfun: KFunction<*>
//                args.add(0, lib)
//                try {
//                    kfun = recall(lib::class, ctx.lbl().text, args)
//                } catch (e: IllegalArgumentException) {
//                    args.add(1, context)
//                    kfun = recall(lib::class, ctx.lbl().text, args)
//                }
//                while (args.size < kfun.parameters.size) {
//                    args.add(null)
//                }
//                try {
//                    kfun.call(*args.toTypedArray()) as JsonNode?
//                } catch (e: InvocationTargetException) {
//                    throw e.targetException
//                }
//            }
//        }
//    }

    override fun visitChain(ctx: JSongParser.ChainContext): JsonNode? {
        context = visit(ctx.lhs)
        return visit(ctx.rhs)
    }


    override fun visitContext(ctx: JSongParser.ContextContext): JsonNode? {
        return context
    }

    override fun visitDefine(ctx: JSongParser.DefineContext): JsonNode {
        return visit(ctx.`fun`())!!
    }

    override fun visitDescendants(ctx: JSongParser.DescendantsContext): JsonNode? {
        val result = ArrayNode(objectMapper.nodeFactory)
        if (context is ObjectNode) {
            result.addAll(descendants(context))
        }
        return reduce(result)
    }

    override fun visitDiv(ctx: JSongParser.DivContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().divide(rhs.decimalValue(), mathContext))
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
        val result = ArrayNode(objectMapper.nodeFactory)
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

    /**
     * Return the [FunctionNode] from [ctx] content matching
     *
     * `fun:  ('fun'|'function') '(' ('$' lbl (',' '$' lbl)*)? ')' '{' exp '}';`.
     */
    override fun visitFun(
        ctx: JSongParser.FunContext
    ): FunctionNode {
        return FunctionNode(
            ctx.lbl().map { lbl -> lbl.text },
            ctx.exp().text
        )
    }

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| '$' lbl                                       #recall`.
     *
     * @return the [JsonNode] mapped in [varMap] with the `lbl` key, it can be `null`.
     *
     * @see visitSet
     */
    override fun visitGet(
        ctx: JSongParser.GetContext
    ): JsonNode? {
        return varMap[ctx.lbl().text]
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

    override fun visitIfe(ctx: JSongParser.IfeContext): JsonNode? {
        return when (lib.boolean(visit(ctx.prd)).booleanValue()) {
            true -> visit(ctx.pos)
            else -> visit(ctx.neg)
        }
    }

    override fun visitIn(ctx: JSongParser.InContext): JsonNode? {
        val lhs = reduce(visit(ctx.lhs))
        val rhs = expand(visit(ctx.rhs))
        return BooleanNode.valueOf(rhs.contains(lhs))
    }

    override fun visitJsong(ctx: JSongParser.JsongContext): JsonNode? {
//        var result: JsonNode? = null
//        ctx.exp().forEach { exp ->
//            result = visit(exp)
//        }
//        return result
        return ctx.exp()?.let { exp -> visit(exp) }
    }

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| fun '(' (exp (',' exp)*)? ')'                 #lambda`
     *
     * where fun is
     *
     * `fun:  ('fun'|'function') '(' ('$' lbl (',' '$' lbl)*)? ')' '{' exp '}';`.
     *
     * @return the [JsonNode] can be `null`.
     *
     * @throws FunctionNotFoundException if no function has the `lbl` name.
     * @throws FunctionTypeException if the `lbl` resolves in a node that is not a function.
     *
     * @see args
     * @see call
     * @see visitFun
     */
    @Throws(
        FunctionNotFoundException::class,
        FunctionTypeException::class
    )
    override fun visitLambda(
        ctx: JSongParser.LambdaContext
    ): JsonNode? {
        return when (val func = visit(ctx.`fun`())) {
            null -> throw FunctionNotFoundException(ctx.`fun`().text)
            else -> {
                val args = args(ctx.exp())
                when (func) {
                    is FunctionNode -> call(func, args)
                    else -> throw FunctionTypeException.forNode(func)
                }
            }
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
        val result = objectMapper.nodeFactory.arrayNode()
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
        var result = objectMapper.nodeFactory.arrayNode()
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
        ctxMap[ctx.lbl().text] = result
        result = ArrayNode(objectMapper.nodeFactory)
        for (i in 0 until ratio) {
            result.addAll(lhs)
        }
        return reduce(result)
    }

    override fun visitMappos(ctx: JSongParser.MapposContext): JsonNode? {
        val result = objectMapper.nodeFactory.arrayNode()
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
        posMap[ctx.lbl().text] = result
        return reduce(result)
    }

    override fun visitMod(ctx: JSongParser.ModContext): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().remainder(rhs.decimalValue()))
    }

    /**
     * Return the [DecimalNode] from the [ctx] content matching
     *
     * `| lhs = exp '*' rhs = exp                       #mul`.
     *
     * LHS an RHS are converted to numbers calling [JSONataFunctionLibrary.number].
     *
     * @return LHS **multiplied** by RHS.
     */
    override fun visitMul(ctx: JSongParser.MulContext): DecimalNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().multiply(rhs.decimalValue()))
    }

    /**
     * Return the [BooleanNode] from the [ctx] content matching
     *
     * `| lhs = exp '!=' rhs = exp                      #ne`
     *
     * LHS an RHS are converted to booleans calling [JSONataFunctionLibrary.boolean].
     *
     * @return `true` if LHS and RHS are **not equal**.
     */
    override fun visitNe(
        ctx: JSongParser.NeContext
    ): BooleanNode {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        return BooleanNode.valueOf(lhs != rhs)
    }

    /**
     * Return the [NullNode] instance from the [ctx] content matching
     *
     * `nil: NULL;`.
     */
    override fun visitNil(
        ctx: JSongParser.NilContext
    ): JsonNode? {
        return NullNode.instance
    }

    /**
     * Return the [DecimalNode] from the [ctx] content matching
     *
     * `num: NUMBER;`
     */
    override fun visitNum(
        ctx: JSongParser.NumContext
    ): DecimalNode {
        return DecimalNode(ctx.text.toBigDecimal())
    }

    /**
     * Return the [ObjectNode] from the [ctx] content matching
     *
     * ```
     * obj
     *     : '{' pair (',' pair)* '}'
     *     | '{' '}'
     *     ;
     *
     * pair
     *     : key = exp ':' value = exp
     *     ;
     * ```
     * The `key` of each `pair` is cast to string calling [JSONataFunctionLibrary.string];
     * if `key` is `null` the index of the `pair` property of `obj` is used.
     *
     * If the `value` of `pair` is `null` then is set to [NullNode].
     *
     */
    override fun visitObj(
        ctx: JSongParser.ObjContext
    ): ObjectNode {
        val result = ObjectNode(objectMapper.nodeFactory)
        ctx.pair().forEachIndexed { index, pair ->
            val key = visit(pair.key)?.let { lib.string(it).asText() } ?: index.toString()
            val value = visit(pair.value) ?: NullNode.instance
            result.set<JsonNode>(key, value)
        }
        return result
    }

    /**
     * Return the [BooleanNode] from the [ctx] content matching
     *
     * `| lhs = exp OR   rhs = exp                      #or`.
     *
     * LHS an RHS are converted to booleans calling [JSONataFunctionLibrary.boolean].
     *
     * @return `true` if LHS or RHS is true.
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
     * Return the [RangeNode] from the [ctx] content matching
     *
     * ```
     * range
     *     : min = exp '..' max = exp
     *     ;
     * ```
     *
     * The `min` and `max` limits are converted to numbers calling [JSONataFunctionLibrary.number].
     *
     * @see visitRanges
     */
    override fun visitRange(
        ctx: JSongParser.RangeContext
    ): RangeNode {
        val min = lib.number(visit(ctx.min))
        val max = lib.number(visit(ctx.max))
        return RangeNode.of(min.decimalValue(), max.decimalValue(), objectMapper.nodeFactory)
    }

    /**
     * Return the [RangesNode] from the [ctx] content matching
     *
     * `| '[' range (',' range)* ']'                        #ranges`
     *
     * @see visitRange
     */
    override fun visitRanges(
        ctx: JSongParser.RangesContext
    ): RangesNode {
        val result = RangesNode(objectMapper.nodeFactory)
        ctx.range().forEach { range ->
            result.add(visit(range))
        }
        return result
    }


    /**
     * Return the [RegexNode] from the [ctx] content matching
     *
     * `| REGEX                                         #regex`.
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
     * Return the last context from the [ctx] content matching
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
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| '$' lbl ':=' exp                              #set`.
     *
     * @return the [JsonNode] result of `exp` evaluation, it can be `null`:
     *         the returned value is stored in the [varMap] with `lbl` key.
     *
     * @see visitGet
     */
    override fun visitSet(
        ctx: JSongParser.SetContext
    ): JsonNode? {
        return visit(ctx.exp()).also {
            varMap[ctx.lbl().text] = it
        }
    }

    /**
     * Return the [DecimalNode] from [ctx] content matching
     *
     * `| lhs = exp '-' rhs = exp                       #sub`.
     *
     * LHS and RHS are cast to numbers calling [JSONataFunctionLibrary.number].
     *
     * @return LHS - RHS.
     *
     * @see [JSONataFunctionLibrary.number]
     */
    override fun visitSub(
        ctx: JSongParser.SubContext
    ): DecimalNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().subtract(rhs.decimalValue()))
    }

    /**
     * Return the [TextNode] from the [ctx] content matching
     *
     * ```
     * txt
     *     : STRING
     *     ;
     * ```.
     */
    override fun visitTxt(
        ctx: JSongParser.TxtContext
    ): TextNode {
        return TextNode(ctx.text.substring(1, ctx.text.length - 1))
    }

} //~ Processor

