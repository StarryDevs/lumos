import org.fusesource.jansi.Ansi.ansi
import org.starry.lumos.core.parser.buffer
import org.starry.lumos.core.parser.marked
import org.starry.lumos.core.parser.parse
import org.starry.lumos.core.parser.rule
import org.starry.lumos.core.tokenizer.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.math.BigDecimal
import kotlin.time.measureTime

fun macro(ctx: MutableMap<String, () -> BigDecimal>) = rule("Macro") {
    val name = identifier()
    punctuation("=")
    marked {
        val tokens = buffer.tokens()
        name.getName() to {
            TokenBuffer(tokens.processTokens(ctx)).parse(ExpressionParser.EXPRESSION.expression).calculate()
        }
    }
}

fun run(command: String, ctx: MutableMap<String, () -> BigDecimal>): List<String> {
    if (command.trim().isEmpty()) return emptyList()
    val chunks = mutableListOf<MutableList<Token>>(mutableListOf())
    val tokens = SimpleTokenizer(command).tokenize().asIterable()
    for (token in tokens) {
        if (token is PunctuationToken && token.getPunctuation() == ';') chunks.add(mutableListOf())
        else if (token is WhitespaceToken) continue
        else chunks.last().add(token)
    }
    val keys = mutableListOf<String>()
    for (chunk in chunks) {
        if (chunk.isEmpty()) continue
        val (key, value) = TokenBuffer(chunk).ignoreWhitespace().parse(macro(ctx))
        ctx[key] = value
        keys += key
    }
    return keys
}

fun debug(command: String, ctx: MutableMap<String, () -> BigDecimal>) {
    val keys = run(command, ctx)
    val rawResult = ctx.map {
        val (key, function) = it
        val value: Result<BigDecimal>
        val time = measureTime { value = runCatching { function() } }
        Triple(time, key, value)
    }.toMutableList()
    if (rawResult.isEmpty()) return
    val result = rawResult.toMutableList()
    result.removeIf { it.second in keys }
    result.sortBy { it.first }
    for (key in keys) result.addFirst(rawResult.first { it.second == key })
    val maxKeySize = ctx.keys.maxOfOrNull { it.length } ?: return
    val maxTimeSize = result.map { it.first }.maxOfOrNull { it.toString().length } ?: return
    for ((time, key, value) in result) {
        println(
            ansi().fgBrightBlue()
                .a("[${time.toString() + " ".repeat(maxTimeSize - time.toString().length)}] ")
                .fgYellow()
                .a("${key + " ".repeat(maxKeySize - key.length)} ")
                .fgCyan()
                .a("=> ")
                .let {
                    if (value.isSuccess) it.fgGreen().a(value.getOrNull()) else it.fgRed().a(value.exceptionOrNull())
                }
        )
    }
}

fun Iterable<Token>.processTokens(ctx: Map<String, () -> BigDecimal>): Iterable<Token> {
    return map { if (it is IdentifierToken && it.getName() in ctx) NumericToken(ctx[it.getName()]!!()) else it }
}

fun main() {
    //AnsiConsole.systemInstall()
    val ctx = mutableMapOf<String, () -> BigDecimal>()
    while (true) {
        val originalCtx = ctx.toList()
        try {
            print(ansi().fgBlue().a("> "))
            val line = (readlnOrNull() ?: break).takeUnless { it.trim().isEmpty() } ?: continue
            if (line.startsWith("$")) run(line.removePrefix("$"), ctx)
            else if (line.startsWith("#")) {
                debug(line.removePrefix("#"), ctx)
                ctx.clear()
                ctx.putAll(originalCtx)
            }
            else if (line == ".clear") ctx.clear()
            else {
                val result: Expression
                val time = measureTime {
                    result = TokenBuffer(
                        SimpleTokenizer(line)
                            .tokenize()
                            .asIterable()
                            .processTokens(ctx)
                    ).ignoreWhitespace().parse(ExpressionParser.EXPRESSION.expression)
                }
                println(ansi().fgBrightGreen().a("[$time] ").fgYellow().a(result).fgCyan().a(" => ").fgBlue().a(result.calculate()))
            }
        } catch (throwable: Throwable) {
            val stream = ByteArrayOutputStream()
            throwable.printStackTrace(PrintStream(stream))
            println(ansi().fgRed().a(stream.toByteArray().decodeToString()))
        }
    }
}
