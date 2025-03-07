package org.starry.lumos.core.tokenizer

import java.math.BigDecimal

interface Token {
    fun raw(): String
}

object EmptyToken : Token {
    override fun raw() = ""
}

inline fun <reified T : Token> Token.isToken(raw: String? = null) =
    (raw ?: raw()) == raw() && T::class.isInstance(this)

open class UnknownToken(private val raw: String) : Token {

    override fun raw() = raw

    override fun toString() = "Token(${raw()})"

}

class StringToken(raw: String, private val parsed: String) : UnknownToken(raw) {

    fun getString() = parsed
    override fun toString() = "${super.toString()}{ $parsed }"

    operator fun unaryPlus() = parsed

}

class NumericToken(raw: String, private val parsed: BigDecimal) : UnknownToken(raw) {

    constructor(parsed: BigDecimal) : this(parsed.toPlainString(), parsed)

    fun getNumber() = parsed
    override fun toString() = "${super.toString()}{ $parsed }"

    operator fun unaryPlus() = parsed

}

class PunctuationToken(private val raw: Char) : UnknownToken(raw.toString()) {

    fun getPunctuation() = raw

    operator fun unaryPlus() = raw

}

class WhitespaceToken(raw: String) : UnknownToken(raw) {

    operator fun unaryPlus() = raw()

}

class NullToken : UnknownToken("null") {

    operator fun unaryPlus() = null

}

class BooleanToken(raw: String, private val parsed: Boolean = raw == "true") : UnknownToken(raw) {

    fun getBoolean(): Boolean = parsed


    operator fun unaryPlus() = parsed
    operator fun unaryMinus() = BooleanToken((!parsed).toString(), !parsed)

}

class IdentifierToken(raw: String, private val parsed: String, private val isStringify: Boolean = false) :
    UnknownToken(raw) {

    fun getName() = parsed
    fun isStringify() = isStringify

    operator fun unaryPlus() = parsed

}
