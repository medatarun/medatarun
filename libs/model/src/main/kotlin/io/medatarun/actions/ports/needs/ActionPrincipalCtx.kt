package io.medatarun.actions.ports.needs

interface ActionPrincipalCtx {
    fun ensureIsAdmin()
    fun ensureSignedIn(): MedatarunPrincipal
    fun ensureIsEmbeddedUser(): MedatarunPrincipal
    val actor: MedatarunPrincipal?
}