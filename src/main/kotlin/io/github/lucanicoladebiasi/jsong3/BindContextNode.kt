package io.github.lucanicoladebiasi.jsong3

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode

class BindContextNode(mapper: ObjectMapper): ArrayNode(mapper.nodeFactory)