package org.jsong

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

/**
 * https://docs.jsonata.org/numeric-functions#parseinteger
 */
class _TestBooleanFunctions {

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - boolean false`() {
        assertFalse(_JSong.of("\$boolean(false)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - boolean true`() {
        assertTrue(_JSong.of("\$boolean(true)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string empty`() {
        assertFalse(_JSong.of("\$boolean(\"\")").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string non-empty`() {
        assertTrue(_JSong.of("\$boolean(\"Hello world!\")").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number zero`() {
        assertFalse(_JSong.of("\$boolean(0)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number non-zero`() {
        assertTrue(_JSong.of("\$boolean(1)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - null`() {
        assertFalse(_JSong.of("\$boolean(null)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array empty`() {
        assertFalse(_JSong.of("\$boolean([ ])").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array some true`() {
        assertTrue(_JSong.of("\$boolean([0, 1, 0])").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array all false`() {
        assertFalse(_JSong.of("\$boolean([0, 0, 0])").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object empty`() {
        val actual = _JSong.of("\$boolean([{}])").evaluate()
        assertFalse(actual!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object not empty`() {
        assertTrue(_JSong.of("\$boolean([{\"flag\": false}])").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Disabled("todo: functions")
    @Test
    fun `$boolean() - function`() {
    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @Test
    fun `$not() - false`() {
        assertTrue(_JSong.of("\$not(false)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @Test
    fun `$not() - true`() {
        assertFalse(_JSong.of("\$not(true)").evaluate()!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - false`() {
        val actual = _JSong.of("\$exists(Other.Nothing)").evaluate(_TestResources.address)
        assertFalse(actual!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - true`() {
        val actual = _JSong.of("\$exists(Address.City)").evaluate(_TestResources.address)
        assertTrue(actual!!.booleanValue())
    }
}