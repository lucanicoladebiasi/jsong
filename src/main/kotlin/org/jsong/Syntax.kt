package org.jsong

/**
 * This class collects constant describing the syntax of JSONata functions implemented in [Functions],
 * these are used in the emssages of the trown exceptions.
 */
object Syntax {

    const val BASE64_DECODE = "\$base64decode(str)"
    const val BASE64_ENCODE = "\$base64encode(str)"
    const val CONTAINS = "\$contains(str, pattern)"
    const val DECODE_URL = "\$decodeUrl(str)"
    const val DECODE_URL_COMPONENT = "\$decodeUrlComponent(str)"
    const val ENCODE_URL = "\$encodeUrl(str)"
    const val ENCODE_URL_COMPONENT = "\$encodeUrlComponent(str)"
    const val EVAL = "\$eval(expr [, context])"
    const val JOIN = "\$join(array[, separator])"
    const val LENGTH_OF = "\$length(str)"
    const val LOWERCASE = "\$lowercase(str)"
    const val MATCH = "\$match(str, pattern [, limit])"
    const val PAD = "\$pad(str, width [, char])"
    const val REPLACE = "\$replace(str, pattern, replacement [, limit])"
    const val SPLIT = "\$split(str, separator [, limit])"
    const val STRING_OF = "\$string(arg, prettify)"
    const val SUBSTRING = "\$substring(str, start[, length])"
    const val SUBSTRING_AFTER = "\$substringAfter()"
    const val SUBSTRING_BEFORE = "\$substringBefore(str, chars)"
    const val TRIM = "\$trim(str)"
    const val UPPERCASE = "\$uppercase(str)"

    const val ABS = "\$abs(number)"
    const val CEIL = "\$ceil(number)"
    const val FLOOR = "\$floor(number)"
    const val FORMAT_BASE = "\$formatBase(number [, radix])"
    const val FORMAT_INTEGER = "\$formatInteger(number, picture)"
    const val FORMAT_NUMBER = "\$formatNumber(number, picture [, options])"
    const val NUMBER = "\$number(arg)"
    const val PARSE_INTEGER = "\$parseInteger(string, picture)"
    const val POWER = "\$power(base, exponent)"
    const val ROUND = "\$round(number [, precision])"
    const val SQRT = "\$sqrt(number)"
    const val RANDOM = "\$random()"

    const val AVERAGE = "\$average(array)"
    const val MAX = "\$max(array)"
    const val MIN = "\$min(array)"
    const val SUM = "\$sum(array)"

    const val BOOLEAN_OF = "\$boolean(arg)"
    const val EXISTS = "\$exists(arg)"
    const val NOT = "\$not(arg)"

    const val APPEND = "\$append(array1, array2)"
    const val COUNT = "\$count(array)"
    const val DISTINCT = "\$distinct(array)"
    const val REVERSE = "\$reverse(array)"
    const val SHUFFLE = "\$shuffle(array)"
    const val SORT = "\$sort(array [, function])"
    @Suppress("unused")
    const val ZIP = "\$zip(array1, ...)"

    const val ASSERT = "\$assert(condition, message)"
    @Suppress("unused")
    const val EACH = "\$each(object, function)"
    const val ERROR = "\$error(message)"
    const val KEYS = "\$keys(object)"
    const val LOOKUP = "\$lookup(object, key)"
    const val MERGE = "\$merge(array<object>)"
    @Suppress("unused")
    const val SIFT = "\$sift(object, function)"
    const val SPREAD = "\$spread(object)"
    const val TYPE = "\$type(value)"

    const val FROM_MILLIS = "\$fromMillis(number [, picture [, timezone]])"
    const val MILLIS = "\$millis()"
    const val NOW = "\$now([picture [, timezone]])"
    const val TO_MILLIS = "\$toMillis(timestamp [, picture])"

}