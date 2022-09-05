package org.jsong

import com.fasterxml.jackson.databind.node.DecimalNode
import java.math.BigDecimal

class TimeNode(value: BigDecimal): DecimalNode(value) {

}