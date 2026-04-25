package io.medatarun.security

interface SecurityRuleCtx {
    fun isSignedIn(): Boolean
    fun isAdmin(): Boolean
    fun getPermissions(): Set<AppPermissionKey>
    fun ensurePermission(wantedPermission: AppPermission): SecurityRuleEvaluatorResult = ensurePermission(wantedPermission.key)
    fun ensurePermission(wantedPermission: AppPermissionKey): SecurityRuleEvaluatorResult
}
