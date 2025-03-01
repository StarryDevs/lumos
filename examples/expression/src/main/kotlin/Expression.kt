import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max


interface Expression {
    fun calculate(): BigDecimal
}

class UnaryExpression(val operator: String, val expression: Expression) : Expression {
    override fun toString() = "$operator$expression"
    override fun calculate(): BigDecimal {
        val value = expression.calculate()
        return when (operator) {
            "-" -> -value
            "+" -> value
            "@" -> value.toBigInteger().toBigDecimal()
            else -> throw IllegalArgumentException("Invalid operation")
        }
    }
}

class NthRootExpression(val numberExpression: Expression, val nthExpression: Expression, val precision: BigDecimal = BigDecimal("0.000001")) : Expression {

    override fun calculate(): BigDecimal {
        val number = numberExpression.calculate()
        val nth = nthExpression.calculate()
        var left = BigDecimal.ZERO
        var right: BigDecimal = number
        var mid: BigDecimal = number.divide(BigDecimal(2))
        while (right.subtract(left) > precision) {
            val powN = mid.pow(nth.toInt())
            if (powN == number) return mid
            else if (powN < number) left = mid
            else right = mid
            mid = left.add(right).divide(BigDecimal(2))
        }
        return mid
    }

    override fun toString() = "nrt($numberExpression, $nthExpression)"

}

class BinaryExpression(val left: Expression, val right: Expression, val operation: String) : Expression {
    override fun toString() = "($left $operation $right)"
    override fun calculate(): BigDecimal {
        val a = left.calculate()
        val b = right.calculate()
        val bigDecimalTrue = BigDecimal.ONE
        val bigDecimalFalse = BigDecimal.ZERO
        return when (operation) {
            "^" -> a.pow(b.toInt())
            "*" -> if (a.toInt() == 0 || b.toInt() == 0) bigDecimalFalse else a * b
            "/" -> a.divide(b, max(a.scale(), 1) + max(b.scale(), 1), RoundingMode.HALF_EVEN)
            "%" -> a % b
            "+" -> a + b
            "-" -> a - b
            ">" -> if (a > b) bigDecimalTrue else bigDecimalFalse
            "<" -> if  (a < b) bigDecimalTrue else bigDecimalFalse
            ">=" -> if (a >= b) bigDecimalTrue else bigDecimalFalse
            "<=" -> if (a <= b) bigDecimalTrue else bigDecimalFalse
            "==" -> if (a == b) bigDecimalTrue else bigDecimalFalse
            "!=" -> if (a != b) bigDecimalTrue else bigDecimalFalse
            "?:" -> if (a == bigDecimalFalse) b else a
            "<-" -> a.movePointLeft(b.toInt())
            "->" -> a.movePointRight(b.toInt())
            "&&" -> if (a == bigDecimalFalse) a else b
            "||" -> if (a != bigDecimalFalse) a else b
            else -> throw UnsupportedOperationException()
        }
    }
}

enum class Identifier(val value: BigDecimal) : Expression {
    PI(Math.PI.toBigDecimal()), TAU(Math.TAU.toBigDecimal()), RANDOM(Math.random().toBigDecimal()), E(Math.E.toBigDecimal()), NULL(0.toBigDecimal());

    override fun calculate() = value
    override fun toString() = value.toString()
}

class Literal(val value: BigDecimal) : Expression {
    override fun toString() = value.toString()
    override fun calculate() = value
}
