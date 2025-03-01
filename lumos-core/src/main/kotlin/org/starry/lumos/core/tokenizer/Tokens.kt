package org.starry.lumos.core.tokenizer

import org.starry.lumos.core.parser.AbstractParser
import org.starry.lumos.core.parser.buffer
import org.starry.lumos.core.parser.include
import org.starry.lumos.core.parser.operator.optional
import org.starry.lumos.core.parser.rule

context(AbstractParser<*>)
inline fun <reified T> Token.expect() = this.also {
    if (!T::class.isInstance(this))
        throw makeError("Expected token: ${T::class}, bot got ${this::class}")
} as T

context(AbstractParser<*>)
fun <T : Token> T.expect(raw: String) = this.also {
    if (!this.isToken<Token>(raw)) throw makeError("Invalid token '${this.raw()}', did you mean '$raw'?")
}

context(AbstractParser<*>)
fun <T : Token> T.expectIdentifier(identifier: String) = expect<IdentifierToken>().expect(identifier)

context(AbstractParser<*>)
fun <T : Token> T.expectPunctuation(character: Char) = expect<PunctuationToken>().expect(character.toString())

context(AbstractParser<*>)
inline fun <reified T> token() = buffer.get().expect<T>()

context(AbstractParser<*>)
fun numeric() = buffer.get().expect<NumericToken>()

context(AbstractParser<*>)
fun identifier(name: String? = null) = buffer.get().expect<IdentifierToken>().let {
    if (name != null) it.expect(name)
    else it
}

context(AbstractParser<*>)
fun punctuation(punctuations: String) = punctuations.map { buffer.get().expectPunctuation(it) }

context(AbstractParser<*>)
fun boolean() = buffer.get().expect<BooleanToken>()

context(AbstractParser<*>)
fun whitespace(optional: Boolean = true) = include(
    rule {
        buffer.get().expect<WhitespaceToken>()
    }.optional()
).getOrNull().let {
    if (!optional) it!!
    else it
}
