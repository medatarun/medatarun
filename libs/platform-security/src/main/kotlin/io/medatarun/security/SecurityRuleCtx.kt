package io.medatarun.security

interface SecurityRuleCtx {
    fun isSignedIn(): Boolean
    fun isAdmin(): Boolean
    fun getPermissions(): List<AppPermission>
    fun ensurePermission(wantedPermission: AppPermission): SecurityRuleEvaluatorResult
}
