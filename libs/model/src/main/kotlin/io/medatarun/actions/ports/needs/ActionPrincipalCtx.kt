package io.medatarun.actions.ports.needs

interface ActionPrincipalCtx {
    fun ensureIsAdmin()
    fun ensureSignedIn(): MedatarunPrincipal
    val actor: MedatarunPrincipal?
}