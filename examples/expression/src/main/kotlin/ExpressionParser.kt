import org.starry.lumos.core.parser.*
import org.starry.lumos.core.parser.operator.ChoiceOperator
import org.starry.lumos.core.parser.operator.choose
import org.starry.lumos.core.parser.operator.optional
import org.starry.lumos.core.parser.operator.repeat
import org.starry.lumos.core.tokenizer.IdentifierToken
import org.starry.lumos.core.tokenizer.numeric
import org.starry.lumos.core.tokenizer.punctuation
import org.starry.lumos.core.tokenizer.token

enum class ExpressionParser(private val enableBinaryExpression: Boolean) {

    BASE_EXPRESSION(false), EXPRESSION(true);

    companion object {
        @JvmField
        val BINARY_EXPRESSION_OPERATORS = arrayOf(
            arrayOf("^"),
            arrayOf("*", "/", "%"),
            arrayOf("+", "-"),
            arrayOf("<-", "->"),
            arrayOf(">", "<", ">=", "<="),
            arrayOf("==", "!="),
            arrayOf("&&"),
            arrayOf("||"),
            arrayOf("?:")
        ).reversed()

        @JvmField
        val UNARY_EXPRESSION_OPERATORS = arrayOf("-", "+", "@")

        @JvmField
        val UNARY_EXPRESSION_OPERATOR = choose(*UNARY_EXPRESSION_OPERATORS.map { rule { punctuation(it) } }.toTypedArray())

        @JvmField
        val FLATTEN_BINARY_EXPRESSION_OPERATORS = BINARY_EXPRESSION_OPERATORS.flatMap { it.toList() }.sortedByDescending { it.length }.toTypedArray()

        @JvmField
        val BINARY_EXPRESSION_OPERATOR = choose(*FLATTEN_BINARY_EXPRESSION_OPERATORS.map { rule { punctuation(it) } }.toTypedArray())
    }

    val identifier = identifier()
    val nrtExpression = nrtExpression()
    val expressionProcessor = ExpressionProcessor()
    val bracketExpression = bracketExpression()
    val binaryExpression = binaryExpression()
    val expression = expression()
    val unaryExpression = unaryExpression()
    val literalExpression = literal()
    val baseExpression = baseExpression()

    fun nrtExpression() = rule("NthRootExpression") {
        org.starry.lumos.core.tokenizer.identifier("nrt")
        marked {
            punctuation("(")
            val number = +expression
            punctuation(",")
            val nth = +expression
            punctuation(")")
            NthRootExpression(number, nth)
        }
    }

    fun bracketExpression() = rule("BracketExpression") {
        punctuation("(")
        val expression = +EXPRESSION.expression
        punctuation(")")
        expression
    }

    fun literal(): ParserSequence<Literal> = rule("Literal") {
        Literal(numeric().getNumber())
    }

    fun unaryExpression(): ParserSequence<UnaryExpression> = rule("UnaryExpression") {
        val operator = (+UNARY_EXPRESSION_OPERATOR).map { it.getPunctuation() }.joinToString("")
        val expression = +expression
        UnaryExpression(operator, expression)
    }

    fun baseExpression(): ChoiceOperator<Expression> = choose(nrtExpression, identifier, bracketExpression, literalExpression, unaryExpression)

    fun expression(): ParserSequence<Expression> = rule("Expression") {
        var base = +baseExpression
        if (enableBinaryExpression) base = buildExpressionTree(
            base,
            +binaryExpression.repeat().map { it }
        )
        base
    }

    fun binaryExpression(): ParserSequence<Triple<Expression, String, Int>> = rule("BinaryExpression") {
        val punctuations = (+BINARY_EXPRESSION_OPERATOR.optional()).getOrNull()?.map { it.getPunctuation() }?.joinToString("") ?: "*"
        val expression = +BASE_EXPRESSION.expression
        Triple(
            expression,
            punctuations,
            BINARY_EXPRESSION_OPERATORS.indexOfFirst { it.indexOf(punctuations) != -1 }
        )
    }

    fun identifier() = rule("Identifier") {
        val identifier = token<IdentifierToken>().getName()
        marked {
            when (identifier) {
                "pi", "PI", "π" -> Identifier.PI
                "e", "E" -> Identifier.E
                "2pi", "2PI", "2π" -> Identifier.TAU
                "random", "RANDOM" -> Identifier.RANDOM
                else -> throw makeError("Identifier '$identifier' is not defined")
            }
        }
    }

    fun buildExpressionTree(
        initial: Expression,
        operations: List<Triple<Expression, String, Int>>
    ): Expression {
        return expressionProcessor.buildExpressionTree(initial, operations)
    }

}