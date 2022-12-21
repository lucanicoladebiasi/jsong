package org.jsonic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.TextNode

interface JSONataLibrary {

    // https://docs.jsonata.org/numeric-functions

    fun abs(number: JsonNode?): DecimalNode

    fun ceil(number: JsonNode?): DecimalNode

    fun floor(number: JsonNode?): DecimalNode

    fun formatBase(number: JsonNode?, radix: DecimalNode?): TextNode

    fun formatInteger(number: JsonNode?, picture: TextNode): TextNode

    fun formatNumber(number: JsonNode?, picture: TextNode, options: TextNode?): TextNode

    fun number(arg: JsonNode?): DecimalNode

    fun parseInteger(string: JsonNode?, picture: TextNode): DecimalNode

    fun power(base: JsonNode?, exponent: JsonNode?): DecimalNode

    fun random(): DecimalNode

    fun round(number: JsonNode?, precision: DecimalNode?): DecimalNode

    fun sqrt(number: JsonNode?): DecimalNode


    // https://docs.jsonata.org/numeric-functions

    fun base64decode(str: JsonNode?): TextNode

    fun base64encode(str: JsonNode?): TextNode

    fun contains(str: JsonNode?, pattern: TextNode): BooleanNode

    fun decodeUrl(str: JsonNode?): TextNode

    fun decodeUrlComponent(str: JsonNode?): TextNode

    fun encodeUrl(str: JsonNode?): TextNode

    fun encodeUrlComponent(str: JsonNode?): TextNode

    fun eval(expr: JsonNode?, context: JsonNode?): JsonNode?

    fun join(array: ArrayNode?, separator: TextNode): TextNode

    fun length(str: JsonNode?): DecimalNode

    fun lowercase(str: JsonNode?): TextNode

    fun match(str: JsonNode, pattern: TextNode, limit: DecimalNode?)

    fun pad(str: JsonNode?, width: DecimalNode, char: TextNode): TextNode

    fun replace(str: JsonNode?, pattern: TextNode, replacement: TextNode, limit: DecimalNode): TextNode

    fun split(str: JsonNode?, separator: TextNode, limit: DecimalNode?): ArrayNode

    fun string(arg: JsonNode?, prettify: BooleanNode): TextNode

    fun substring(str: JsonNode?, start: DecimalNode, length: DecimalNode?): TextNode

    fun substringAfter(str: JsonNode?, chars: TextNode): TextNode

    fun substringBefore(str: JsonNode?, chars: TextNode): TextNode

    fun trim(str: JsonNode?): TextNode

    fun uppercase(str: JsonNode?): TextNode


}