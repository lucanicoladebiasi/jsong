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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
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
import kotlin.reflect.full.memberFunctions

/**
 * This class [evaluate]s an expression expressed in the
 * [JSONata](https://docs.jsonata.org/overview.html) language and the JSong grammatical extensions
 * as defined in the `JSong.g4` [ANTLR](https://www.antlr.org/) grammar.
 *
 * * `exp` refers to the expression evaluated, `exp` has `exp` sub-expressions according the grammar defined in the
 * `JSong.g4` file.
 * * `lhs` refer to the Left Hand Side of a binary expression.
 * * `rhs` refer to the Right Hand Side of a binary expression.
 *
 * The methods prefixed with `visit` override the ANTLR visiting pattern:
 * the documentation shows the matched pattern from [JSong.g4]() rules.
 * If the matched pattern begins with the `|`, the pattern is a fragment of a rule with multiple alternatives.
 *
 * **NOTE: Not thread safe.**
 *
 * @property root node going to be evaluated calling [evaluate].
 * If `null` [evaluate] processes its expression, possibly returning a [JsonNode] not `null`.
 *
 * @property varMap registry of the variables, those can be inherited when this `Processor` is created
 * or registered when [evaluate]expression is called.
 * Empty by default.
 *
 * @property mathContext used to operate with `BigDecimal` class.
 * [MathContext.DECIMAL128] by default.
 *
 * @property objectMapper used to parse JSON expressions and create [JsonNode] objects.
 *
 * @property random source of randomness, used in the [lib] instance.
 *
 * @property time instant used when [evaluate] processes its expression and [root].
 *
 * @property lib implementation of [JSONataFunctionLibrary] providing the
 * [JSONata Function Library](https://docs.jsonata.org/overview.html),
 * by default, [mathContext], [objectMapper], [random] and [time] are used to create a new [Library]
 * instance for this processor.
 */
class Processor(
    private val root: JsonNode? = null,
    private val varMap: MutableMap<String, JsonNode?> = mutableMapOf(),
    private val mathContext: MathContext = MathContext.DECIMAL128,
    val objectMapper: ObjectMapper = ObjectMapper(),
    private val random: Random = Random.Default,
    val time: Instant = Instant.now(),
    private val lib: JSONataFunctionLibrary = Library(mathContext, objectMapper, random, time)
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

    /**
     * Reference to the context, [root] first, then the result of previous `exp` evaluation.
     *
     * See the [$ operator](https://docs.jsonata.org/simple) in JSONata documentation.
     */
    private var context = root

    /**
     * Store the index when [visitFilter] and [visitMap], [visitMapCtx], [visitMapPos]  iterate and retrieve
     * variables stored in [varMap].
     */
    private val indexStack = ArrayDeque<Int>()

    /**
     * Flag if the result of each `exp` calling a `visit` method must be [reduce]d.
     *
     * See [Sequence](https://docs.jsonata.org/processing#sequences) and
     * [Array constructors](https://docs.jsonata.org/construction#array-constructors)
     * in the JSONata documentation.
     */
    private var isToReduce: Boolean = true

    /**
     * Register the
     * [Context variable binding](https://docs.jsonata.org/path-operators#-context-variable-binding)
     * in [varMap].
     *
     * @see visitCtx
     * @see visitLbl
     * @see visitMapCtx
     *
     */
    private val ctxSet = mutableSetOf<String>()

    /**
     * Register of the
     * [positional variable binding](https://docs.jsonata.org/path-operators#-positional-variable-binding)
     * in [varMap].
     *
     * @see visitLbl
     * @see visitMapPos
     * @see visitPos
     *
     */
    private val posSet = mutableSetOf<String>()

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
        varMap["$"]?.let { context = it }
        return Processor(context, varMap, mathContext, objectMapper, random, time, lib).evaluate(func.body)
    }

    /**
     * Return the result if calling the [name] method of the [lib] interface with the [args] parameters.
     *
     * @return [JsonNode] can be `null`.
     *
     * @throws AssertionError if [JSONataFunctionLibrary.assert] is called and it raises an exception.
     * @throws FunctionNotFoundException if [name] ( [args] ) method is not found in [lib].
     *
     * @see args
     * @see visitCall
     */
    @Throws(
        AssertionError::class,
        FunctionNotFoundException::class
    )
    private fun call(
        lib: JSONataFunctionLibrary,
        name: String,
        args: MutableList<JsonNode?>
    ): JsonNode? {
        try {
            val method = lib::class.memberFunctions.first { name == it.name }
            val requiredParameters = method.parameters.filter { !it.isOptional }.size - 1
            if (requiredParameters > 0 && args.size < requiredParameters) {
                args.add(0, context)
            }
            while (args.size < method.parameters.size - 1) {
                args.add(null)  // Fill missing args with `null`.
            }
            val result = when (method.parameters.last().isVararg) {
                true -> method.call(lib, args.toTypedArray())
                else -> method.call(lib, *args.toTypedArray())
            }
            return when (result) {
                null -> null
                is JsonNode -> result
                else -> objectMapper.valueToTree(result)
            }
        } catch (e: IllegalArgumentException) {
            throw FunctionNotFoundException(e.message!!)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        } catch (e: NullPointerException) {
            throw FunctionNotFoundException(e.message!!)
        }
    }

    /**
     * Return the [ArrayNode] having as elements
     * each node belonging to the subtree having `node` as root.
     *
     * @see visitDescendants
     */
    private fun descendants(
        node: JsonNode?
    ): ArrayNode {
        val result = objectMapper.nodeFactory.arrayNode()
        node?.fields()?.forEach { field ->
            if (field.value != null) {
                result.addAll(descendants(field.value))
                result.add(field.value)
            }
        }
        return result
    }

    /**
     * Return the result of this [Processor] evaluating `exp`,
     * the result can be `null`.
     *
     * If [root] is not `null`, this method evaluates [exp] applied to the [root] context.
     */
    fun evaluate(
        exp: String
    ): JsonNode? {
        val canon = exp.replace("\\s".toRegex(), " ")  // TODO: ANTLR doesn't skip spaces correctly.
        return visit(JSongParser(CommonTokenStream(JSongLexer(CharStreams.fromString(canon)))).jsong())
    }

    /**
     * Return an [ArrayNode]
     * * empty if [node] is null,
     * * being a [RangesNode] instance if [node] is a [RangesNode] instance,
     * * having the [node] element if [node] isn't an array,
     * * the [node] itself if it is a not empty array.
     *
     * @see visitExpand
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

    /**
     * Return a [JsonNode]
     * * `null` if [node] is `null`,
     * * 'null` if [node] is an empty [ArrayNode],
     * * `node[0]` if [node] is an [ArrayNode] having a single element,
     * * [node] if none of the above conditions are true.
     */
    private fun reduce(
        node: JsonNode?
    ): JsonNode? {
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

    /**
     * Return the [JsonNode] named [fieldName] part of [node] elements.
     * It can be `null` if
     * * [node] is `null`;
     * * [node] is not [ObjectNode].
     * * [node] hasn't any element named [fieldName].
     *
     * @see visitField
     */
    private fun select(
        node: JsonNode?,
        fieldName: String
    ): JsonNode? {
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

    /**
     * Return an [ArrayNode] coping the [array]`[i]` element if [predicates]`[i]` is `true`.
     *
     * @see stretch
     * @see visitFilter
     */
    @Suppress("KDocUnresolvedReference")
    private fun shrink(
        array: ArrayNode,
        predicates: BooleanArray
    ): ArrayNode {
        val value = objectMapper.nodeFactory.arrayNode()
        predicates.forEachIndexed { index, predicate ->
            if (predicate) {
                value.add(array[index])
            }
        }
        return value
    }

    /**
     * Return an [ArrayNode] node where each element of [array] is replicated [size] / size-of-[array]
     * hence the returned array is [size] long.
     *
     * @see shrink
     * @see visitCtx
     * @see visitPos
     */
    private fun stretch(
        array: ArrayNode,
        size: Int
    ): ArrayNode {
        val value = objectMapper.nodeFactory.arrayNode()
        val ratio = size / array.size()
        array.forEach { element ->
            for (i in 0 until ratio) {
                value.add(element)
            }
        }
        return value
    }

    /**
     * Return the [DecimalNode] from the [ctx] content matching
     *
     * `| lhs = exp '+' rhs = exp                       #add`.
     *
     * @return the [sum](https://docs.jsonata.org/numeric-operators#-addition)
     * of `lhs` plus `rhs` cast calling [JSONataFunctionLibrary.number].
     */
    override fun visitAdd(
        ctx: JSongParser.AddContext
    ): DecimalNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().add(rhs.decimalValue()))
    }

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     *  `| '*'   #all`.
     *
     *  See [Wildcard](https://docs.jsonata.org/path-operators#-wildcard) in JSONata documentation.
     *
     *  @return an [ArrayNode] with all the elements properties of the [context] if the [context] is an [ObjectNode],
     *  else `null`.
     */
    override fun visitAll(
        ctx: JSongParser.AllContext
    ): JsonNode? {
        val result = objectMapper.nodeFactory.arrayNode()
        if (context is ObjectNode) {
            context?.fields()?.forEach { field ->
                result.add(field.value)
            }
        }
        return reduce(result)
    }

    /**
     * Return the [BooleanNode] from [ctx] content matching
     *
     * `| lhs = exp '+' rhs = exp                       #add`.
     *
     * @return the boolean `lhs` [and](https://docs.jsonata.org/boolean-operators#and-boolean-and) `rhs`
     * cast calling [JSONataFunctionLibrary.boolean].
     */
    override fun visitAnd(
        ctx: JSongParser.AndContext
    ): JsonNode? {
        val lhs = lib.boolean(visit(ctx.lhs))
        val rhs = lib.boolean(visit(ctx.rhs))
        return BooleanNode.valueOf(lhs.booleanValue() && rhs.booleanValue())
    }

    /**
     * Return the [TextNode] from [ctx] content matching
     *
     * `| lhs = exp '+' rhs = exp                       #add`.
     *
     * @return the `rhs` [appended](https://docs.jsonata.org/other-operators#-concatenation) to `lhs`
     * cast to string calling [JSONataFunctionLibrary.string].
     */
    override fun visitApp(
        ctx: JSongParser.AppContext
    ): TextNode {
        val sb = StringBuilder()
        visit(ctx.lhs)?.let { lhs ->
            sb.append(lib.string(lhs).textValue())
        }
        visit(ctx.rhs)?.let { rhs ->
            sb.append(lib.string(rhs).textValue())
        }
        return TextNode(sb.toString())
    }

    /**
     * Return the [ArrayNode] from the [ctx] content matching
     *
     * `arr: '[' exp (',' exp)* ']' | '[' ']';`.
     */
    override fun visitArr(
        ctx: JSongParser.ArrContext
    ): ArrayNode {
        val result = objectMapper.nodeFactory.arrayNode()
        ctx.exp().forEach { exp ->
            result.add(visit(exp))
        }
        return result
    }

    /**
     * Return the [BooleanNode] from the [ctx] content matching
     *
     * ` boo: TRUE | FALSE;`
     * ` TRUE: 'true';`
     * ` FALSE: 'false';`.
     *
     * @throws [IllegalArgumentException] if the [ctx] doesn't represent a boolean literal expression.
     */
    @Throws(
        IllegalArgumentException::class
    )
    override fun visitBoo(
        ctx: JSongParser.BooContext
    ): BooleanNode {
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
     * where `lbl` is the name of the function to [call](https://docs.jsonata.org/programming#invoking-a-function).
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

    /**
     * Return the [JsonNode] from the [ctx] content matching
     *
     * `| lhs = exp '~>' rhs = exp                      #chain`.
     *
     * @return the [JsonNode] resulting from the [chain](https://docs.jsonata.org/other-operators#-chain)
     * operation between `lhs` and `rhs`.
     * The result can be `null`.
     */
    override fun visitChain(
        ctx: JSongParser.ChainContext
    ): JsonNode? {
        context = visit(ctx.lhs)
        return visit(ctx.rhs)
    }

    /**
     * Return the [JsonNode] from the [ctx] content matching
     *
     * `| '$'   #context`.
     *
     * See [Sequences](https://docs.jsonata.org/processing#sequences) processing in JSONata documentation.
     *
     * @return the [context] during the [evaluate] processing.
     * It can be `null`.
     */
    override fun visitContext(
        ctx: JSongParser.ContextContext
    ): JsonNode? {
        return context
    }

    /**
     * Return the [ArrayNode] from [ctx] content matching
     *
     * `ctx: '@$' lbl;`
     * `lbl: LABEL;`
     * `LABEL: ([a-zA-Z][0-9a-zA-Z]*) | ('`' (.)+? '`');`.
     *
     * @see map
     * @see visitMapCtx
     */
    @Suppress("DuplicatedCode")
    private fun visitCtx(
        ctx: JSongParser.CtxContext,
        lhs: ArrayNode,
        rhs: ArrayNode
    ): ArrayNode {

        ctxSet.forEach { label ->
            varMap[label] = stretch(expand(varMap[label]), rhs.size())
        }
        varMap[ctx.lbl().text] = rhs
        ctxSet.add(ctx.lbl().text)
        val ratio = rhs.size() / lhs.size()
        val result = objectMapper.nodeFactory.arrayNode()
        for (i in 0 until ratio) {
            result.addAll(lhs)
        }
        return result
    }

    /**
     * Return the [JsonNode] from the [ctx] content matching
     *
     * `| fun                                           #define`
     * `fun:  ('fun'|'function') '(' ('$' lbl (',' '$' lbl)*)? ')' '{' exp '}';`.
     *
     * @return the [JsonNode] as [FunctionNode] [defined](https://docs.jsonata.org/programming#defining-a-function)
     * by the `fun` expression.
     */
    override fun visitDefine(
        ctx: JSongParser.DefineContext
    ): JsonNode {
        return visit(ctx.`fun`())!!
    }

    /**
     * Return the [JsonNode] from the [ctx] content matching
     *
     * `| '**'  #descendants`.
     *
     * @return the [JsonNode] as [ArrayNode] when not `null`.
     * The [ArrayNode] has as elements all
     * [descendants](https://docs.jsonata.org/path-operators#-descendants)
     * of [context].
     */
    override fun visitDescendants(
        ctx: JSongParser.DescendantsContext
    ): JsonNode? {
        val result = objectMapper.nodeFactory.arrayNode()
        if (context is ObjectNode) {
            result.addAll(descendants(context))
        }
        return reduce(result)
    }

    /**
     * Return the [DecimalNode] from [ctx] matching
     *
     * `| lhs = exp '/' rhs = exp                       #div`.
     *
     * @return `lhs` [divided](https://docs.jsonata.org/numeric-operators#-division) by `rhs`.
     * cast as numbers calling [JSONataFunctionLibrary.number].
     */
    override fun visitDiv(
        ctx: JSongParser.DivContext
    ): JsonNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().divide(rhs.decimalValue(), mathContext))
    }

    /**
     * Return the [BooleanNode] from [ctx] content matching
     *
     * `| lhs = exp '=' rhs = exp                       #eq`.
     *
     * @return [BooleanNode.TRUE] if `lhs` is
     * [equal](https://docs.jsonata.org/expressions#comparison-expressions)
     * to `rhs`, else [BooleanNode.FALSE].
     */
    override fun visitEq(
        ctx: JSongParser.EqContext
    ): BooleanNode {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)
        return BooleanNode.valueOf(lhs == rhs)
    }

    /**
     * Return the [ArrayNode] from [ctx] content matching
     *
     * `| exp'[' ']'                                    #expand`
     *
     * See [Array constructor](https://docs.jsonata.org/construction#array-constructors) in JSONata documentation.
     *
     * @return the evaluation of `exp` and [expand] it as [ArrayNode],
     * flag [isToReduce] to `false` to avoid to [reduce] the following steps of [evaluate] processing.
     */
    override fun visitExpand(
        ctx: JSongParser.ExpandContext
    ): ArrayNode {
        val result = expand(visit(ctx.exp()))
        isToReduce = false
        return result
    }

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| path     #select``.
     *
     * See [Navigation JSON objects](https://docs.jsonata.org/simple#navigating-json-objects) in JSONata documentation.
     *
     * @return the [JsonNode] selected by the `path`, if any, else `null`.
     */
    override fun visitField(
        ctx: JSongParser.FieldContext
    ): JsonNode? {
        return select(context, ctx.text)
    }

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| lhs = exp'['  rhs = exp ']'                   #filter`.
     *
     * This method calls [shrink] to keep the size of referred contextual and positional variables in scope
     * of the same size of the result.
     *
     * @return the [JsonNode] where `rhs` [filter](https://docs.jsonata.org/path-operators#---filter) the result of
     * `lhs` evaluation.
     * It can be `null`.
     */
    override fun visitFilter(
        ctx: JSongParser.FilterContext
    ): JsonNode? {
        val result = objectMapper.nodeFactory.arrayNode()
        val lhs = expand(visit(ctx.lhs))
        val predicate = BooleanArray(lhs.size())
        lhs.forEachIndexed { index, context ->
            this.context = context
            indexStack.push(index)
            when (val rhs = visit(ctx.rhs)) {
                is NumericNode -> {
                    val value = rhs.asInt()
                    val offset = if (value < 0) lhs.size() + value else value
                    predicate[index] = index == offset
                    if (predicate[index]) {
                        result.add(context)
                    }
                }

                is RangesNode -> {
                    predicate[index] = rhs.indexes.map { it.asInt() }.contains(index)
                    if (predicate[index]) {
                        result.add(context)
                    }
                }

                else -> {
                    predicate[index] = lib.boolean(rhs).asBoolean()
                    if (predicate[index]) {
                        result.add(context)
                    }
                }

            }
            indexStack.pop()
            this.context = null
        }
        ctxSet.forEach { label ->
            varMap[label] = shrink(expand(varMap[label]), predicate)
        }
        posSet.forEach { label ->
            varMap[label] = shrink(expand(varMap[label]), predicate)
        }
        return reduce(result)
    }

    /**
     * Return the [FunctionNode] from [ctx] content matching
     *
     * `fun:  ('fun'|'function') '(' ('$' lbl (',' '$' lbl)*)? ')' '{' exp '}';`.
     *
     * @return the [FunctionNode] [defined](https://docs.jsonata.org/programming#defining-a-function)
     * by the `fun` expression.
     *
     * @see visitDefine
     */
    override fun visitFun(
        ctx: JSongParser.FunContext
    ): FunctionNode {
        return FunctionNode(
            ctx.lbl().map { lbl -> lbl.text }, ctx.exp().text
        )
    }

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| '$' lbl                                       #recall`.
     *
     * @return the [JsonNode]
     * [mapped](https://docs.jsonata.org/programming#variables)
     * in [varMap] with the `lbl` key, it can be `null`.
     *
     * @see visitSet
     */
    override fun visitGet(
        ctx: JSongParser.GetContext
    ): JsonNode? {
        val label = ctx.lbl().text
        val result = varMap[label]
        return when {
            result == null -> {
                when (lib::class.memberFunctions.firstOrNull { label == it.name }) {
                    null -> null
                    else -> call(lib, label, mutableListOf(context))
                }
            }

            posSet.contains(label) -> IntNode(result.indexOf(context) + 1)
            ctxSet.contains(label) -> when (indexStack.isEmpty()) {
                true -> result
                else -> result[indexStack.peek()]
            }

            else -> result
        }
    }

    /**
     * Return the [BooleanNode] from [ctx] content matching
     *
     * `| lhs = exp '>'  rhs = exp                      #gt`.
     *
     * @return [BooleanNode.TRUE] if `lhs` is [
     * greater then](https://docs.jsonata.org/comparison-operators#-greater-than) `rhs`:
     * * if both `lhs` and `rhs` are numbers the comparison is between number,
     * * else both `lhs` and `rhs` are cast to string calling [JSONataFunctionLibrary.string] then the comparison
     * is done between strings.
     */
    override fun visitGt(
        ctx: JSongParser.GtContext
    ): BooleanNode? {
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

    /**
     * Return the [BooleanNode] from [ctx] content matching
     *
     * `| lhs = exp '>=' rhs = exp                      #gte`.
     *
     * @return [BooleanNode.TRUE] if `lhs` is
     * [greater then or equal](https://docs.jsonata.org/comparison-operators#-greater-than-or-equals) `rhs`:
     * * if both `lhs` and `rhs` are numbers the comparison is between number,
     * * else both `lhs` and `rhs` are cast to string calling [JSONataFunctionLibrary.string] then the comparison
     * is done between strings.
     */
    override fun visitGte(
        ctx: JSongParser.GteContext
    ): BooleanNode {
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

    /**
     * Return the [JsonNode] resulting from the evaluation of `yes` if the evaluation of `prd` is `true` else
     * from the evaluation of `no`.
     *
     * `| prd = exp '?' yes = exp ':' no = exp          #ife`.
     *
     * See the [Conditional operator](https://docs.jsonata.org/other-operators#--conditional) in JSONata documentation.
     *
     * @return the [JsonNode] can be `null`.
     */
    override fun visitIfe(
        ctx: JSongParser.IfeContext
    ): JsonNode? {
        return when (lib.boolean(visit(ctx.prd)).booleanValue()) {
            true -> visit(ctx.yes)
            else -> visit(ctx.no)
        }
    }

    /**
     * Return the [BooleanNode] from [ctx] content matching
     *
     * `| lhs = exp 'in' rhs = exp                      #in`
     *
     * @return [BooleanNode.TRUE] if the result of `lhs` evaluation (after [reduce] calling to cast as scalar if possible)
     * [includes](https://docs.jsonata.org/comparison-operators#in-inclusion)
     * the result of `rhs` evaluation (after [expand] to cast as collection).
     */
    override fun visitIn(
        ctx: JSongParser.InContext
    ): BooleanNode {
        val lhs = reduce(visit(ctx.lhs))
        val rhs = expand(visit(ctx.rhs))
        return BooleanNode.valueOf(rhs.contains(lhs))
    }

    /**
     * Return [JsonNode] from [ctx] content matching
     *
     * `jsong: exp? EOF;`.
     *
     * This is the entry point of the visiting pattern.
     *
     * @see evaluate
     */
    override fun visitJsong(
        ctx: JSongParser.JsongContext
    ): JsonNode? {
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
     * See [Defining a function](https://docs.jsonata.org/programming#defining-a-function) in JSonata documentation.
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
        FunctionNotFoundException::class, FunctionTypeException::class
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

    /**
     * Return the [BooleanNode] from [ctx] content matching
     *
     * `| lhs = exp '<'  rhs = exp                      #lt`.
     *
     * @return [BooleanNode.TRUE]if `lhs` is
     * [less then](https://docs.jsonata.org/comparison-operators#-less-than) `rhs`:
     * * if both `lhs` and `rhs` are numbers the comparison is between number,
     * * else both `lhs` and `rhs` are cast to string calling [JSONataFunctionLibrary.string] then the comparison
     * is done between strings.
     */
    override fun visitLt(
        ctx: JSongParser.LtContext
    ): JsonNode? {
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

    /**
     * Return the [BooleanNode] from [ctx] content matching
     *
     * `| lhs = exp '<'  rhs = exp                      #lt`.
     *
     * @return [BooleanNode.TRUE] if `lhs` is
     * [less then or equal](https://docs.jsonata.org/comparison-operators#-less-than-or-equals) `rhs`:
     * * if both `lhs` and `rhs` are numbers the comparison is between number,
     * * else both `lhs` and `rhs` are cast to string calling [JSONataFunctionLibrary.string] then the comparison
     * is done between strings.
     */
    override fun visitLte(
        ctx: JSongParser.LteContext
    ): JsonNode? {
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

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| lhs = exp '.' rhs = exp                       #map`.
     *
     * The method [maps](https://docs.jsonata.org/path-operators#-map) `lhs` according `rhs`.
     *
     *  The syntax results to be consistent with the "dot" notation to access properties of the [context].
     *
     *  @return [JsonNode] can be `null`.
     */
    @Suppress("DuplicatedCode")
    override fun visitMap(ctx: JSongParser.MapContext): JsonNode? {
        val result = objectMapper.nodeFactory.arrayNode()
        when (val lhs = expand(visit(ctx.lhs))) {
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

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| lhs = exp '.' rhs = exp ctx                   #mapCtx`
     * ' ctx: '@$' lbl;`.
     *
     * The method maps `lhs` according `rhs` and it
     * [binds](https://docs.jsonata.org/path-operators#-context-variable-binding) the resulting
     * [context] to the variable named `lbl`.
     *
     * @return the [JsonNode] resulting from `lhs` evaluation is carried on, it can be `null`.
     *
     * @see ctxSet
     * @see varMap
     * @see visitMap
     * @see visitMapCtx
     */
    @Suppress("DuplicatedCode")
    override fun visitMapCtx(
        ctx: JSongParser.MapCtxContext
    ): JsonNode? {
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
        return reduce(visitCtx(ctx.ctx(), lhs, result))
    }

    /**
     * Return the [JsonNode] from [ctx] content matching
     *
     * `| lhs = exp '.' rhs = exp pos                   #mapPos`
     * `pos: '#$' lbl;`.
     *
     * The method maps `lhs` according `rhs` and it
     * [binds](https://docs.jsonata.org/path-operators#-positional-variable-binding)
     * the position of each element [context] in the variable named `lbl`.
     *
     * @see posSet
     * @see varMap
     * @see visitMap
     * @see visitMapPos
     */
    @Suppress("DuplicatedCode")
    override fun visitMapPos(ctx: JSongParser.MapPosContext): JsonNode? {
        val result = objectMapper.nodeFactory.arrayNode()
        when (val lhs = expand(visit(ctx.lhs))) {
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
        visitPos(ctx.pos(), result)
        return reduce(result)
    }

    /**
     * Return the [DecimalNode] from the [ctx] content matching
     *
     * `| lhs = exp '%' rhs = exp                       #mod`.
     *
     * @return the [DecimalNode] [remainder](https://docs.jsonata.org/numeric-operators#-modulo)
     * of the integer division `lhs` divided by `rhs`
     * cast as numbers calling [JSONataFunctionLibrary.number].
     */
    override fun visitMod(
        ctx: JSongParser.ModContext
    ): DecimalNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().remainder(rhs.decimalValue()))
    }

    /**
     * Return the [DecimalNode] from the [ctx] content matching
     *
     * `| lhs = exp '*' rhs = exp                       #mul`.
     *
     * @return the [DecimalNode] [product](https://docs.jsonata.org/numeric-operators#-multiplication)
     * of `lhs` multiplied by `rhs`, those are cast as numbers calling [JSONataFunctionLibrary.number].
     */
    override fun visitMul(
        ctx: JSongParser.MulContext
    ): DecimalNode {
        val lhs = lib.number(visit(ctx.lhs))
        val rhs = lib.number(visit(ctx.rhs))
        return DecimalNode(lhs.decimalValue().multiply(rhs.decimalValue()))
    }

    /**
     * Return the [BooleanNode] from [ctx] matching
     *
     * `| lhs = exp '!=' rhs = exp                      #ne`.
     *
     * @return [BooleanNode.TRUE] if `lhs` is
     * [not equal](https://docs.jsonata.org/comparison-operators#-not-equals)
     * to `rhs`, else [BooleanNode.FALSE].
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
     * @return the boolean `lhs` [or](https://docs.jsonata.org/boolean-operators#or-boolean-or) `rhs`
     * cast calling [JSONataFunctionLibrary.boolean].
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
     * Implement the
     * [positional variable bind](https://docs.jsonata.org/path-operators#-positional-variable-binding)
     * of `lbl` expression for the rule
     *
     * 'pos: '#$' lbl;`.
     *
     * Set the [context] as `lbl` variable in [varMap].
     * Add `lbl` key to [posSet].
     *
     * @return [context], it can be `null`.
     *
     * @see visitMap
     * @see visitMapPos
     */
    @Suppress("DuplicatedCode")
    private fun visitPos(
        ctx: JSongParser.PosContext,
        rhs: ArrayNode
    ) {
        val positional = objectMapper.nodeFactory.arrayNode().addAll(rhs)
        posSet.forEach { label ->
            varMap[label] = stretch(expand(varMap[label]), positional.size())
        }
        varMap[ctx.lbl().text] = positional
        posSet.add(ctx.lbl().text)
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
     * See [Range](https://docs.jsonata.org/numeric-operators#-range) in JSONata documentation.
     *
     * **NOTE: JSONata represents ranges as a sequence of integers from `min` to `max`.
     * JSong uses ranges in the same way JSONata does when the sequence of integers must be applied,
     * but it represents ranges in mathematical terms, hence the `in` operator works correctly
     * when a [DecimalNode] is tested if inside or outside the given range.**
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
     * See [Range](https://docs.jsonata.org/numeric-operators#-range) in JSONata documentation.
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
     * `| REGEX                                         #regex`
     * `REGEX: '/' (.)+? '/' 'i'? 'm'?;`.
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
     * `| '(' exp (';' exp)* ')'`.
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
        return when (ctx.exp().text.contains("~>")) {
            true -> FunctionNode(listOf("$"), ctx.exp().text, objectMapper.nodeFactory).also {
                varMap[ctx.lbl().text] = it
            }

            else -> visit(ctx.exp()).also {
                varMap[ctx.lbl().text] = it
            }
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
     * `txt: STRING;`.
     */
    override fun visitTxt(
        ctx: JSongParser.TxtContext
    ): TextNode {
        return TextNode(ctx.text.substring(1, ctx.text.length - 1))
    }

} //~ Processor

