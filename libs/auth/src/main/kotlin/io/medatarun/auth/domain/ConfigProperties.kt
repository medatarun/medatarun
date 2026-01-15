package io.medatarun.auth.domain

import io.medatarun.lang.io.medatarun.lang.config.ConfigPropertyDescription

enum class ConfigProperties(
    override val key: String,
    override val type: String,
    override val defaultValue: String,
    override val description: String
) : ConfigPropertyDescription {
    /**
     * Property to get prefilled secret
     */
    BootstrapSecret(
        key = "medatarun.auth.bootstrap.secret",
        type = "String",
        defaultValue = "<generated>",
        description = """Bootstrap secret used to obtain the initial administrator access.
In most environments, this value does not need to be set.
If not provided, Medatarun generates a random secret at startup and prints it in the logs.

In environments where startup logs are not accessible (for example when running in containers or managed platforms), you should explicitly set this value. Minimum length is 20 characters."""
    ),

    Issuers(
        key = "medatarun.auth.jwt.trusted.issuers",
        type = "CSV",
        defaultValue = "",
        description = "Comma separated list of accepted JWT issuers. Each name will be used to get further configuration."
    ),
    IssuerIssuer(
        key = "medatarun.auth.jwt.trusted.issuer.xxx.issuer",
        type = "String",
        defaultValue = "",
        description = "Required for every issuer. In the property name, replace `xxx` with one your issuer names defined in `medatarun.auth.jwt.trusted.issuers`. The value must be the exact name of `iss` claim as it appears in the JWT."
    ),
    IssuerJWKS(
        key = "medatarun.auth.jwt.trusted.issuer.xxx.jwks",
        type = "URL",
        defaultValue = "",
        description = "Required for every issuer. Absolute URL of your issuer's JWKS. You can often get it from its `.well-known/openid-configuration` (`jwks_uri` in this document's JSON). Otherwise ask your provider. It is the URL where we'll download the public keys of your issuer to validate their tokens.",
    ),
    IssuerAlgorithms(
        key = "medatarun.auth.jwt.trusted.issuer.xxx.algorithms",
        type = "CSV",
        defaultValue = "",
        description = "Comma separated list of algorithms that this issuer will sign its JWT with. We only support `RS256` and `ES256`. If you let this empty (or don't provide it), it is assumed `RS256` only."
    ),
    IssuerAudiences(
        key = "medatarun.auth.jwt.trusted.issuer.xxx.audiences",
        type = "CSV",
        defaultValue = "",
        description = "Comma separated list of audiences that we need to check. If you let this empty (or not defined), no audience checks will be done. If you set some audiences, it is required that your JWT contains an `aud` with at least one of the audiences in this configuration."
    ),
    JwksCacheDuration(
        key = "medatarun.auth.jwt.jwks.cache.duration",
        type = "Integer",
        defaultValue = "600",
        description = "Duration (in seconds) we keep public keys in our local cache. Each JWT needs to be validated against your issuer public key. This is the time we keep the keys until we ask for a fresh one."
    ),
    UIOidcAuthority(
        key = "medatarun.auth.ui.oidc.authority",
        type = "URL",
        defaultValue = "",
        description = "If you let this empty, it is assumed Medatarun's UI will use our built-in OIDC provider. Then this value will depend on your `medatarun.public.base.url` and you don't have to take care of it. If specified, our UI will call your IdP to sign your users (OpenId Connect). In this case you need to specify your OIDC provider's authority URL (from which we will auto-discover its OIDC configuration).",
        ),
    UiOidcClientId(
        key = "medatarun.auth.ui.oidc.clientid",
        type = "String",
        defaultValue = "",
        description = "If you have configured an OIDC authority and registered Medatarun as a client application, this is the name of the _client id_ you used to register Medatarun in your OIDC Identity Provider."
    );

    fun withName(name: String): String {
        return key.replace(".xxx.", ".$name.")
    }
}