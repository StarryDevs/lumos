package org.starry.lumos.core.parser.operator

import org.starry.lumos.core.exception.SyntaxError
import org.starry.lumos.core.parser.AbstractParser
import org.starry.lumos.core.parser.includeWithState

class ChoiceOperator<T : Any>(vararg val choices: AbstractParser<out T>) : AbstractParser<T>() {

    override fun parse(): T {
        var exception: Throwable? = null
        for (choice in choices) {
            val (_, result) = includeWithState(choice.optional())
            if (result.isSuccess) return result.getOrNull() ?: throw makeError("Invalid syntax")
            else if (result.exceptionOrNull() is SyntaxError) {
                exception = result.exceptionOrNull()
                break
            }
        }
        if (exception != null) throw exception
        else throw makeError("Invalid syntax")
    }

}

fun <T : Any> choose(vararg choices: AbstractParser<out T>) = ChoiceOperator(*choices)

@Suppress("UNCHECKED_CAST")
infix fun <T : Any> AbstractParser<out T>.or(other: AbstractParser<out T>): ChoiceOperator<T> {
    return if (this is ChoiceOperator<*>) choose(*this.choices, other) as ChoiceOperator<T>
    else if (other is ChoiceOperator<*>) choose(this, *other.choices) as ChoiceOperator<T>
    else choose(this, other)
}

infix fun <T : Any> ChoiceOperator<T>.not(filter: (AbstractParser<out T>) -> Boolean) =
    ChoiceOperator(*this.choices.filter(filter).toTypedArray())

inline fun <reified T : Any> ChoiceOperator<T>.not() = not {
    it !is T && !T::class.isInstance(it)
}
