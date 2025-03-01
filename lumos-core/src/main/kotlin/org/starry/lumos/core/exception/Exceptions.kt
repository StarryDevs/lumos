@file:Suppress("CanBeParameter")

package org.starry.lumos.core.exception

import org.starry.lumos.core.parser.AbstractParser
import org.starry.lumos.core.tokenizer.TokenBuffer
import org.starry.lumos.core.tokenizer.Tokenizer
import java.net.URI
import java.nio.CharBuffer

open class BaseParsingException(message: String?, val uri: URI, cause: Throwable? = null) :
    RuntimeException(message, cause)

class TokenizerException(
    message: String?,
    val buffer: CharBuffer,
    uri: URI,
    val tokenizer: Tokenizer,
    cause: Throwable? = null
) :
    BaseParsingException(message, uri, cause = cause)

open class ParserException(
    private val inputMessage: String?,
    val buffer: TokenBuffer,
    val parser: AbstractParser<*>,
    val state: AbstractParser.State,
    private val inputCause: Throwable? = null
) :
    BaseParsingException(inputMessage, state.uri, cause = inputCause) {

    private val tokens = buffer.tokens()

    override val message: String = "An error occurred while parsing ${parser.getName()}"
    override val cause: Throwable
        get() = BaseParsingException(inputMessage, uri, inputCause)

}

class SyntaxError(
    inputMessage: String?,
    buffer: TokenBuffer,
    parser: AbstractParser<*>,
    state: AbstractParser.State,
    inputCause: Throwable? = null
) : ParserException(inputMessage, buffer, parser, state, inputCause)

