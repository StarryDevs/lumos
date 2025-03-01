import org.starry.lumos.core.parser.parse
import org.starry.lumos.core.tokenizer.SimpleTokenizer
import org.starry.lumos.core.tokenizer.TokenBuffer
import org.starry.lumos.core.tokenizer.ignoreWhitespace
import kotlin.time.measureTime

fun main() = JsonParser.run {
    while (true) {
        try {
            print("> ")
            val line = (readlnOrNull() ?: break).takeUnless { it.trim().isEmpty() } ?: continue
            val result: JsonNode
            val time = measureTime {
                result =
                    TokenBuffer(SimpleTokenizer(line).tokenize().asIterable()).ignoreWhitespace().parse(JSON)
            }
            println("[$time] ${result.read()}")
        } catch (throwable: Throwable) {
            throwable.printStackTrace(System.out)
        }
    }
}