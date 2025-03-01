package org.starry.lumos.core.tokenizer

import org.starry.lumos.core.exception.TokenizerException
import java.net.URI
import java.nio.CharBuffer

interface Tokenizer {

    val buffer: CharBuffer
    val uri: URI

    fun reset() = this.also {
        buffer.position(0)
    }

    fun makeError(message: String) = TokenizerException(message, buffer, uri, this)

    fun tokenize(): Sequence<Token>

}