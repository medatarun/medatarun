package io.medatarun.auth.internal.oidc

enum class OidcClientOrigin {
    /**
     * Client is internal to medatarun (cli, ui, etc.)
     */
    INTERNAL,

    /**
     * Client had been registered with DCRP protocol
     */
    DCRP
}
