package org.starry.lumos.core.parser.operator

import org.starry.lumos.core.parser.AbstractParser
import org.starry.lumos.core.parser.include
import org.starry.lumos.core.parser.rule
import org.starry.lumos.core.tokenizer.punctuation
import org.starry.lumos.core.tokenizer.whitespace

class ListOperator<T : Any>(
    val element: AbstractParser<T>,
    val prefix: String? = "(",
    val suffix: String? = ")",
    val separator: String? = ",",
    val allowSeparatorEnd: Boolean = false
) : AbstractParser<List<T>>() {

    val part = rule {
        whitespace()
        if (separator != null) punctuation(separator)
        whitespace()
        return@rule include(element)
    }

    override fun parse(): List<T> {
        if (prefix != null) punctuation(prefix)
        val elements = mutableListOf<T>()
        whitespace()
        val first = include(element.optional()).getOrNull()
        whitespace()
        if (first != null) {
            elements += first
            while (true) {
                whitespace()
                val result = include(part.optional()).getOrNull()
                whitespace()
                if (result == null) break
                else elements += result
            }
        }
        if (allowSeparatorEnd && separator != null) {
            whitespace()
            include(
                rule { punctuation(separator) }.optional()
            )
            whitespace()
        }
        if (suffix != null) {
            punctuation(suffix)
        }
        return elements
    }

}


fun <T : Any> AbstractParser<T>.list(
    prefix: String? = "(",
    suffix: String? = ")",
    separator: String? = ",",
    allowSeparatorEnd: Boolean = false
) = ListOperator(this, prefix, suffix, separator, allowSeparatorEnd)

fun <T : Any, R : Any> AbstractParser<out Iterable<T>>.mapEach(block: (T) -> R) = rule {
    include(this@mapEach).map(block)
}

fun <T : Any, R : Any> AbstractParser<out Iterable<T>>.mapEachIndexed(block: (index: Int, T) -> R) = rule {
    include(this@mapEachIndexed).mapIndexed(block)
}
