import org.starry.lumos.core.parser.ParserSequence
import org.starry.lumos.core.parser.marked
import org.starry.lumos.core.parser.operator.choose
import org.starry.lumos.core.parser.operator.list
import org.starry.lumos.core.parser.rule
import org.starry.lumos.core.tokenizer.*

object JsonParser {

    @JvmField val JSON = json()
    @JvmField val JSON_ENTRY = jsonEntry()
    @JvmField val JSON_VALUE = jsonValue()
    @JvmField val JSON_ARRAY = jsonArray()
    @JvmField val JSON_OBJECT = jsonObject()

    fun json(): ParserSequence<JsonNode> = rule("Json") {
        +choose(JSON_OBJECT, JSON_ARRAY, JSON_VALUE)
    }

    fun jsonEntry(): ParserSequence<Pair<String, JsonNode>> = rule("JsonEntry") {
        marked {
            val string = token<StringToken>()
            punctuation(":")
            val value = +JSON
            string.getString() to value
        }
    }

    fun jsonValue(): ParserSequence<JsonValue> = rule("JsonValue") {
        JsonValue(
            when (val token = token<Token>()) {
                is NumericToken -> token.getNumber()
                is StringToken -> token.getString()
                is BooleanToken -> token.getBoolean()
                is NullToken -> null
                else -> throw makeError("Invalid token")
            }
        )
    }

    fun jsonArray(): ParserSequence<JsonArray> = rule("JsonArray") {
        val array = JsonArray()
        val values = +JSON.list("[", "]")
        array.addAll(values)
        array
    }

    fun jsonObject(): ParserSequence<JsonObject> = rule("JsonObject") {
        val obj = JsonObject()
        val entries = +JSON_ENTRY.list("{", "}")
        obj.putAll(entries)
        obj
    }

}
