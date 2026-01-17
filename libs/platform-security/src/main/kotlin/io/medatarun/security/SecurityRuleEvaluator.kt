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
    fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult
}

sealed interface SecurityRuleEvaluatorResult {
    class Ok: SecurityRuleEvaluatorResult
    class Error(val msg: String): SecurityRuleEvaluatorResult
}
