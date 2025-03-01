interface JsonNode {
    fun read(): Any?
}

class JsonValue(private val value: Any?) : JsonNode {
    override fun read() = value
}

class JsonObject : JsonNode, MutableMap<String, JsonNode> by mutableMapOf() {
    override fun read() = mapValues { it.value.read() }
}

class JsonArray : JsonNode, MutableList<JsonNode> by mutableListOf() {
    override fun read() = map { it.read() }
}
