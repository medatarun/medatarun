package io.medatarun.actions.ports.needs

interface MedatarunPrincipal {
    val issuer: String
    val sub: String
    val isAdmin: Boolean
}
