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
     * Human-readable short name used by UIs.
     */
    val name: String

    /**
     * Human-readable explanation of what this rule enforces.
     * Used in operator-facing UIs and docs.
     */
    val description: String

    /**
     * Dynamically evaluate the rule with its context
     */
    fun evaluate(ctx: SecurityRuleCtx): SecurityRuleEvaluatorResult

    /**
     * Returns a list of permissions associated to the rule.
     * It helps frontend or client applications to filter what the user
     * can do or not, even if not a lot of details, but roughtly to avoid
     *  displaying things the current user cannot do.
     */
    fun associatedRequiredPermissions(): List<AppPermissionKey> = emptyList()
}

sealed interface SecurityRuleEvaluatorResult {
    class Ok: SecurityRuleEvaluatorResult
    class AuthenticationError(val msg: String): SecurityRuleEvaluatorResult
    class AuthorizationError(val msg: String): SecurityRuleEvaluatorResult
    class Error(val msg: String): SecurityRuleEvaluatorResult
}
