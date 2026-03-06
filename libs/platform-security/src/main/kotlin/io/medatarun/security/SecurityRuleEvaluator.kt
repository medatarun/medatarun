package io.medatarun.security

/**
 * Security rule evaluator allows evaluating a security context
 * and tell if a rule matches or not
 */
interface SecurityRuleEvaluator {
    /**
     * Security rule key
     */
    val key: String

    /**
     * Human-readable explanation of what this rule enforces.
     * Used in operator-facing UIs and docs.
     */
    val description: String
    fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult
}

sealed interface SecurityRuleEvaluatorResult {
    class Ok: SecurityRuleEvaluatorResult
    class Error(val msg: String): SecurityRuleEvaluatorResult
}
