package org.starry.lumos.core.parser

import org.starry.lumos.core.exception.ParserException
import org.starry.lumos.core.tokenizer.TokenBuffer
import java.net.URI


abstract class AbstractParser<T : Any>(name: String? = null) {

    private val name: String = name ?: this::class.java.simpleName

    fun getName() = name

    data class State(
        var uri: URI,
        var buffer: TokenBuffer,
        var parent: AbstractParser<*>? = null
    )

    private var state: State? = null
    open fun state(state: State?) = this.also { this.state = state }
    open fun stateOrNull(): State? = state
    open fun state() = stateOrNull()!!

    abstract fun parse(): T

    open fun makeError(message: String? = null, cause: Throwable? = null) = state().let {
        ParserException(message, it.buffer, this, it, cause)
    }

}

abstract class Parser<T : Any>(name: String? = null) : AbstractParser<T>(name) {


    operator fun <R : Any> AbstractParser<R>.unaryPlus() = this@Parser.include(this@unaryPlus)

}

class ParserSequence<T : Any>(name: String? = null, private val block: ParserSequence<T>.() -> T) : Parser<T>(name) {

    override fun parse(): T = block()

}
