package org.starry.lumos.core.parser.operator

import org.starry.lumos.core.parser.AbstractParser
import org.starry.lumos.core.parser.buffer
import org.starry.lumos.core.parser.include
import org.starry.lumos.core.parser.rule

class OptionalOperator<T : Any>(val parser: AbstractParser<T>, val rollback: Boolean = true) : AbstractParser<Result<T>>() {

    override fun parse(): Result<T> {
        val position = buffer.position()
        try {
            val result = Result.success(include(parser))
            return result
        } catch (throwable: Throwable) {
            if (rollback) {
                buffer.position(position)
            }
            return Result.failure(throwable)
        }
    }

}

fun <R : Any> AbstractParser<R>.optional(rollback: Boolean = true) = OptionalOperator(this, rollback)

fun <T : Any> AbstractParser<Result<T>>.orElse(block: (Throwable) -> T) = rule {
    include(this@orElse).getOrElse(block)
}

fun <T : Any> AbstractParser<Result<T>>.orElse(parser: AbstractParser<T>) = rule {
    include(this@orElse).getOrElse {
        include(parser)
    }
}

