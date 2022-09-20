package org.jsong

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

/**
 * https://docs.jsonata.org/numeric-functions#parseinteger
 */
class TestBooleanFunctions {

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - boolean false`() {
        assertFalse(JSong.of("\$boolean(false)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - boolean true`() {
        assertTrue(JSong.of("\$boolean(true)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string empty`() {
        assertFalse(JSong.of("\$boolean(\"\")").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string non-empty`() {
        assertTrue(JSong.of("\$boolean(\"Hello world!\")").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number zero`() {
        assertFalse(JSong.of("\$boolean(0)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number non-zero`() {
        assertTrue(JSong.of("\$boolean(1)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - null`() {
        assertFalse(JSong.of("\$boolean(null)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array empty`() {
        assertFalse(JSong.of("\$boolean([ ])").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array some true`() {
        assertTrue(JSong.of("\$boolean([0, 1, 0])").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array all false`() {
        assertFalse(JSong.of("\$boolean([0, 0, 0])").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object empty`() {
        val actual = JSong.of("\$boolean([{}])").evaluate()
        assertFalse(actual!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object not empty`() {
        assertTrue(JSong.of("\$boolean([{\"flag\": false}])").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Disabled
    @Test
    fun `$boolean() - function`() {
    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @Test
    fun `$not() - false`() {
        assertTrue(JSong.of("\$not(false)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @Test
    fun `$not() - true`() {
        assertFalse(JSong.of("\$not(true)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - false`() {
        val actual = JSong.of("\$exists(Other.Nothing)").evaluate(TestResources.address)
        assertFalse(actual!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - true`() {
        val actual = JSong.of("\$exists(Address.City)").evaluate(TestResources.address)
        assertTrue(actual!!.booleanValue())
    }
}