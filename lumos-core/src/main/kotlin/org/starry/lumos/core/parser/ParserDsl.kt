package org.starry.lumos.core.parser

import org.starry.lumos.core.Lumos
import org.starry.lumos.core.exception.SyntaxError
import org.starry.lumos.core.tokenizer.TokenBuffer

var AbstractParser<*>.state: AbstractParser.State
    get() = state()
    set(value) = Unit.also { state(value) }

var AbstractParser<*>.buffer: TokenBuffer
    get() = state.buffer
    set(value) = Unit.also { state(state().copy(buffer = value)) }

fun <T : Any> TokenBuffer.parse(parser: AbstractParser<T>) = rule {
    include(parser)
}.apply {
    state(AbstractParser.State(Lumos.BLANK_URI, this@parse))
}.parse()

fun <R : Any> AbstractParser<*>.include(parser: AbstractParser<R>): R = includeWithState(parser).second

fun <R : Any> AbstractParser<*>.includeWithState(parser: AbstractParser<R>): Pair<AbstractParser.State, R> {
    val origin = parser.stateOrNull()
    parser.state(this.stateOrNull()?.copy(parent = this))
    val result = parser.parse()
    val resultState = parser.state()
    parser.state(origin)
    return resultState to result
}

fun <T : Any> rule(name: String? = null, block: ParserSequence<T>.() -> T) = ParserSequence(name, block)

fun <T : Any, R : Any> AbstractParser<T>.map(block: (T) -> R) = rule {
    block(include(this@map))
}

fun <T : Any> AbstractParser<T>.marked(block: AbstractParser<T>.() -> T): T {
    try {
        return block()
    } catch (cause: Throwable) {
        throw SyntaxError("Invalid syntax", buffer, this, state(), cause)
    }
}
