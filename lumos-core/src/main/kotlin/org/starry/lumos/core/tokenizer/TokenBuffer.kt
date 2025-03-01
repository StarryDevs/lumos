package org.starry.lumos.core.tokenizer

import java.nio.BufferUnderflowException

class TokenBuffer(private val tokens: List<Token>) : Iterable<Token> {

    constructor(tokenizer: Tokenizer) : this(tokenizer.reset().tokenize().toList())
    constructor(iterable: Iterable<Token>) : this(iterable.toList())

    private var position: Int = 0

    fun tokens() = tokens.slice(position until tokens.size)

    override fun iterator() = tokens.iterator()

    fun all() = tokens

    fun get() = get(position++)
    operator fun get(position: Int) = tokens.getOrNull(position) ?: throw BufferUnderflowException()

    fun position() = position
    fun position(position: Int) = this.also { this.position = position }
    fun back(step: Int = 1) = position(position() - step)

    fun next(offset: Int = 1) = position(position() + offset)
    fun hasNext() = position < tokens.size

    override fun toString() = tokens().toString()

}

fun Tokenizer.toTokenBuffer() = TokenBuffer(this)
fun TokenBuffer.ignoreWhitespace() = TokenBuffer(all().filterNot { it is WhitespaceToken })
