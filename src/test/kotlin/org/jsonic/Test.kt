package org.jsonic

import org.jsong.TestResources
import org.junit.jupiter.api.Test

class Test {

    @Test
    fun `Returns a JSON string`() {
        val expression = "Phone.number[0]"
        val processor = Processor(TestResources.address)

        val l = processor.lib::class
        l.members.forEach { k ->

        }
    }

}