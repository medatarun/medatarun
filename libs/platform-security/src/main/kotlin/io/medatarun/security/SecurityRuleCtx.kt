package io.medatarun.security

interface SecurityRuleCtx {
    fun isSignedIn(): Boolean
    fun isAdmin(): Boolean
    fun getRoles(): List<AppPrincipalRole>
}
