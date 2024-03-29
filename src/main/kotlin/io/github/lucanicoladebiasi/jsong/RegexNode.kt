/**
 * MIT License
 *
 * Copyright (c) 2023 Luca Nicola Debiasi
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
package io.github.lucanicoladebiasi.jsong

import com.fasterxml.jackson.databind.node.TextNode

/**
 * This class represents a [regular expression](https://docs.jsonata.org/regex)
 * as defined in JSonata documentation.
 *
 * The constructor scans for flags/options and represent them in the [regex] property.
 *
 * Immutable.
 *
 * @param pattern in JSONata notation: the pattern is between [DELIMITER], flags follow.
 */
class RegexNode(pattern: String): TextNode(pattern) {

    companion object {

        /**
         * Literal flag to enable
         * [equivalence by canonical](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex-option/-c-a-n-o-n_-e-q.html)
         * decomposition.
         */
        const val CANON_EQ = 'e'

        /**
         * Literal flag to permit whitespace and
         * [comments](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex-option/-c-o-m-m-e-n-t-s.html)
         * in patterns,
         */
        const val COMMENTS = 'c'

        /**
         * Literal flag to enable the mode, when the expression `.`
         * [matches any character](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex-option/-d-o-t_-m-a-t-c-h-e-s_-a-l-l.html),
         * including a line terminator.
         */
        const val DOT_MATCHES_ALL = 'a'

        /**
         * JSONata documentation prescribes regular expression pattern are enclosed between slashes.
         */
        const val DELIMITER = '/'

        /**
         * Literal flag to enable
         * [case-insensitive matching](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex-option/-i-g-n-o-r-e_-c-a-s-e.html).
         * Case comparison is Unicode-aware.
         *
         */
        const val IGNORE_CASE = 'i'

        /**
         * [Literal](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex-option/-l-i-t-e-r-a-l.html)
         * flag to set metacharacters or escape sequences in the input sequence will be given no special meaning.
         */
        const val LITERAL = 'l'

        /**
         * Literal flag to enables
         * [multiline](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex-option/-m-u-l-t-i-l-i-n-e.html)
         * mode.
         */
        const val MULTILINE = 'm'

        /**
         * Literal flag to enable
         * [Unix lines mode](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex-option/-u-n-i-x_-l-i-n-e-s.html).
         * In this mode, only the '\n' is recognized as a line terminator.
         */
        const val UNIX_LINES = 'u'


    } //~ companion

    /**
     * The constructor parses pattern and flags to set [regex] property.
     */
    val regex: Regex

    init {
        regex = when {
            pattern.contains(DELIMITER) -> {
                val canonical = pattern.substringAfter(DELIMITER)
                when {
                    canonical.contains(DELIMITER) -> {
                        val options = canonical.substringAfter(DELIMITER)
                        val optionSet = mutableSetOf<RegexOption>()
                        if (options.contains(CANON_EQ)) optionSet.add(RegexOption.CANON_EQ)
                        if (options.contains(COMMENTS)) optionSet.add(RegexOption.COMMENTS)
                        if (options.contains(DOT_MATCHES_ALL)) optionSet.add(RegexOption.DOT_MATCHES_ALL)
                        if (options.contains(IGNORE_CASE)) optionSet.add(RegexOption.IGNORE_CASE)
                        if (options.contains(LITERAL)) optionSet.add(RegexOption.LITERAL)
                        if (options.contains(UNIX_LINES)) optionSet.add(RegexOption.UNIX_LINES)
                        if (options.contains(MULTILINE)) optionSet.add(RegexOption.MULTILINE)
                        Regex(canonical.substringBefore(DELIMITER), optionSet)
                    }
                    else -> Regex(canonical)
                }
            }
            else -> Regex(pattern)
        }
    }

} //~ RegexNode