class ExpressionProcessor {
    fun buildExpressionTree(initial: Expression, operations: List<Triple<Expression, String, Int>>): Expression {
        val nodes = mutableListOf(initial)
        val operators = mutableListOf<Pair<String, Int>>()

        for ((operand, operator, priority) in operations) {
            while (operators.isNotEmpty() && shouldReduce(operators.last().second, priority)) {
                reduce(nodes, operators)
            }

            nodes.add(operand)
            operators.add(Pair(operator, priority))
        }

        while (operators.isNotEmpty()) {
            reduce(nodes, operators)
        }

        return nodes.first()
    }

    // 判断是否需要归约
    private fun shouldReduce(topPriority: Int, currentPriority: Int): Boolean {
        return topPriority >= currentPriority
    }

    // 执行归约操作
    private fun reduce(nodes: MutableList<Expression>, operators: MutableList<Pair<String, Int>>) {
        if (nodes.size < 2 || operators.isEmpty()) return

        val right = nodes.removeAt(nodes.lastIndex)
        val left = nodes.removeAt(nodes.lastIndex)
        val (operator, _) = operators.removeAt(operators.lastIndex)

        nodes.add(BinaryExpression(left, right, operator))
    }
}