package org.jsonic

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
        assertFalse(Processor().evaluate("\$boolean(false)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - boolean true`() {
        assertTrue(Processor().evaluate("\$boolean(true)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string empty`() {
        assertFalse(Processor().evaluate("\$boolean(\"\")")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - string non-empty`() {
        assertTrue(Processor().evaluate("\$boolean(\"Hello world!\")")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number zero`() {
        assertFalse(Processor().evaluate("\$boolean(0)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - number non-zero`() {
        assertTrue(Processor().evaluate("\$boolean(1)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - null`() {
        assertFalse(Processor().evaluate("\$boolean(null)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array empty`() {
        assertFalse(Processor().evaluate("\$boolean([ ])")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array some true`() {
        assertTrue(Processor().evaluate("\$boolean([0, 1, 0])")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - array all false`() {
        assertFalse(Processor().evaluate("\$boolean([0, 0, 0])")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object empty`() {
        val actual = Processor().evaluate("\$boolean([{}])")
        assertFalse(actual!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#boolean
     */
    @Test
    fun `$boolean() - object not empty`() {
        assertTrue(Processor().evaluate("\$boolean([{\"flag\": false}])")!!.booleanValue())
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
        assertTrue(Processor().evaluate("\$not(false)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#not
     */
    @Test
    fun `$not() - true`() {
        assertFalse(Processor().evaluate("\$not(true)")!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - false`() {
        val actual = Processor(TestResources.address).evaluate("\$exists(Other.Nothing)")
        assertFalse(actual!!.booleanValue())
    }

    /**
     * https://docs.jsonata.org/boolean-functions#exists
     */
    @Test
    fun `$exists() - true`() {
        val actual = Processor(TestResources.address).evaluate("\$exists(Address.City)")
        assertTrue(actual!!.booleanValue())
    }
}