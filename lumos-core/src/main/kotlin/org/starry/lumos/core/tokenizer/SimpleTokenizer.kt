package org.starry.lumos.core.tokenizer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.starry.lumos.core.Lumos
import org.starry.lumos.core.exception.BaseParsingException
import java.net.URI
import java.nio.CharBuffer

sealed class LexResult {
    data object Pass : LexResult()
    data class Success(val token: Token) : LexResult()
}

open class SimpleTokenizer(
    override val buffer: CharBuffer,
    override val uri: URI = Lumos.BLANK_URI,
    var punctuations: String = DEFAULT_PUNCTUATIONS
) : Tokenizer {

    var disableSpecialNumbers = true
    var disableWhitespace = true
    var disableExponential = true

    constructor(content: CharSequence, uri: URI = Lumos.BLANK_URI, punctuations: String = DEFAULT_PUNCTUATIONS) : this(
        CharBuffer.wrap(content),
        uri,
        punctuations
    )

    companion object {
        const val DEFAULT_PUNCTUATIONS = "+-*/<>,.;:?()[]{}!%|&=@^"
    }

    private val objectMapper = jacksonObjectMapper()

    fun lexBlockComment() {
        buffer.position(buffer.position() + 2)
        var last: Char = buffer.get()
        while (true) {
            val current = buffer.get()
            if (current == '/' && last == '*') break
            last = current
        }
    }

    override fun tokenize(): Sequence<Token> = sequence {
        var whitespace = ""
        while (true) {
            if (buffer.isEmpty()) break
            val character = buffer.get()
            if (character.isWhitespace() || character in "\n\r") {
                whitespace += character
                continue
            } else {
                whitespace = ""
                if (!disableWhitespace) yield(WhitespaceToken(whitespace))
            }
            buffer.position(buffer.position() - 1)
            val fallbackPosition = buffer.position()
            val lexResultBegin = lexOnceBegin()
            if (lexResultBegin is LexResult.Success) {
                yield(lexResultBegin.token)
                continue
            } else buffer.position(fallbackPosition)
            if (character == '/' && buffer.getOrNull(buffer.position() + 1) == '*') {
                lexBlockComment()
                continue
            } else if (character.lowercaseChar() == 'i' && buffer.getOrNull(buffer.position() + 1) == '"') {
                buffer.get()
                val string = lexString()
                yield(IdentifierToken(raw = string.raw(), parsed = string.getString(), isStringify = true))
            } else if (character.lowercaseChar() == 'r' && buffer.getOrNull(buffer.position() + 1) == '"') {
                buffer.get()
                val raw = lexString().raw()
                yield(StringToken(raw = objectMapper.writeValueAsString(raw), parsed = raw))
            } else if (character.isJavaIdentifierStart()) yield(lexIdentifier())
            else if (character == '"') yield(lexString())
            else if (isNumeric()) yield(lexNumeric())
            else if (character in punctuations) yield(PunctuationToken(buffer.get()))
            else {
                val lexResultEnd = lexOnceEnd()
                if (lexResultEnd is LexResult.Success) yield(lexResultEnd.token)
                else throw makeError("Invalid syntax")
            }
        }
    }.filter { it != EmptyToken }

    open fun lexOnceBegin(): LexResult = LexResult.Pass
    open fun lexOnceEnd(): LexResult = LexResult.Pass

    open fun lexNumeric(): Token {
        return if (disableSpecialNumbers) lexDecimalNumber()
        else if (buffer.startsWith("0x") || buffer.startsWith("0X")) lexHexNumber()
        else if (buffer.startsWith("0b") || buffer.startsWith("0B")) lexBinaryNumber()
        else if (buffer.startsWith("0o") || buffer.startsWith("0O")) lexOctalNumber()
        else lexDecimalNumber()
    }

    open fun lexDecimalNumber(): Token {
        var raw = ""
        var character: Char
        while (true) {
            try {
                character = buffer.get()
            } catch (_: Throwable) {
                break
            }
            if (!(character.isDigit() || (character == '.' && '.' !in raw) || (!disableExponential && character.lowercase() == "e" && 'e' !in raw.lowercase()) || (!disableExponential && character in "+-" && (raw.last()
                    .lowercase() == "e")))
            ) {
                buffer.position(buffer.position() - 1)
                break
            }
            raw += character
        }
        if (raw.isEmpty()) throw makeError("Invalid or unexpected token")
        return NumericToken(raw, raw.toBigDecimal())
    }

    open fun lexOctalNumber(): Token {
        buffer.position(buffer.position() + 2)
        var raw = "0o"
        var character: Char
        while (true) {
            try {
                character = buffer.get()
            } catch (_: Throwable) {
                break
            }
            if (!(character.isDigit() && character !in "89")) {
                buffer.position(buffer.position() - 1)
                break
            }
            raw += character
        }
        if (raw.length == 2) throw makeError("Invalid or unexpected token")
        return NumericToken(raw, raw.slice(2..<raw.length).toBigInteger(8).toBigDecimal())
    }

    open fun lexBinaryNumber(): Token {
        buffer.position(buffer.position() + 2)
        var raw = "0b"
        var character: Char
        while (true) {
            try {
                character = buffer.get()
            } catch (_: Throwable) {
                break
            }
            if (character !in "01") {
                buffer.position(buffer.position() - 1)
                break
            }
            raw += character
        }
        if (raw.length == 2) throw makeError("Invalid or unexpected token")
        return NumericToken(raw, raw.slice(2..<raw.length).toBigInteger(2).toBigDecimal())
    }

    open fun lexHexNumber(): Token {
        buffer.position(buffer.position() + 2)
        var raw = "0x"
        var character: Char
        while (true) {
            try {
                character = buffer.get()
            } catch (_: Throwable) {
                break
            }
            if (!(character.isDigit() || character in 'a'..'f' || character in 'A'..'F')) {
                buffer.position(buffer.position() - 1)
                break
            }
            raw += character
        }
        if (raw.length == 2) throw makeError("Invalid or unexpected token")
        return NumericToken(raw, raw.slice(2..<raw.length).toBigInteger(16).toBigDecimal())
    }

    open fun isNumeric(): Boolean {
        val character = buffer.get(buffer.position())
        if (character.isDigit()) return true
        if (character == '.') {
            try {
                if (buffer.get(buffer.position() + 1).isDigit()) return true
            } catch (_: Throwable) {
            }
        }
        return false
    }

    open fun lexString() = runCatching {
        var raw = buffer.get().toString()
        while (buffer.get(buffer.position()) != '"') {
            if (buffer.get(buffer.position()) == '\\') {
                raw += buffer.get()
            }
            raw += buffer.get()
        }
        raw += buffer.get()
        try {
            return@runCatching StringToken(raw, objectMapper.readValue<String>(raw))
        } catch (_: Throwable) {
            throw makeError("Invalid string literal: $raw")
        }
    }.let {
        if (it.isFailure && it.exceptionOrNull() !is BaseParsingException) throw makeError("Invalid or unexpected token")
        else if (it.isFailure) throw it.exceptionOrNull()!!
        else it.getOrThrow()
    }

    open fun lexIdentifier(): Token {
        var identifier = "${buffer.get()}"
        try {
            while (true) {
                val character = buffer.get()
                if (character.isJavaIdentifierPart()) identifier += character
                else {
                    buffer.position(buffer.position() - 1)
                    break
                }
            }
        } catch (_: Throwable) {
        }
        return when (identifier) {
            "null" -> NullToken()
            "true", "false" -> BooleanToken(identifier)
            else -> IdentifierToken(identifier, identifier)
        }
    }

}
