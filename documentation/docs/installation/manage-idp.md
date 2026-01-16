---
sidebar_position: 40
---
# Connect your identity provider

:::note
This step is optional.
:::

Medatarun works out of the box with its built-in identity provider.  
You only need this page if you want Medatarun to validate JWTs issued by an external provider and/or enable SSO for the browser UI.

Connecting an external identity provider does not change Medatarun’s security model.
Medatarun always secures access by validating JWTs; only the token issuer changes.

This page assumes you are familiar with Medatarun’s authentication as described in
[Manage users](./manage-users.md) and how to [configure Medatarun](./configuration.md).

This page also assumes basic familiarity with authentication concepts.
A JWT (JSON Web Token) is a signed token used to authenticate and authorize requests.
An identity provider (IdP) is the system that issues these tokens after user authentication.
A client_id identifies Medatarun as a registered application in that provider.

## JWT and actors

When a JWT token is received and approved, Medatarun [actors](./actors.md) are updated accordingly.

External actors are created lazily, the first time a valid token issued by a trusted provider is presented.
Once created, they persist and can be managed like any other actor.

## Configure trusted JWT issuers

_Trusted JWT issuers_ are systems allowed to issue tokens that Medatarun will accept.

This configuration applies to API, CLI, MCP, and all token-based access.
Medatarun does not issue external tokens. It only validates tokens issued by trusted systems.


The following example intentionally shows multiple providers to illustrate different configurations.

```properties
medatarun.auth.jwt.trusted.issuers=google,azure
medatarun.auth.jwt.trusted.issuer.google.issuer=https://accounts.google.com
medatarun.auth.jwt.trusted.issuer.google.jwks=https://www.googleapis.com/oauth2/v3/certs
medatarun.auth.jwt.trusted.issuer.google.audiences=client-id-1, client-id-2
medatarun.auth.jwt.trusted.issuer.google.algorithms=RS256
medatarun.auth.jwt.trusted.issuer.azure.issuer=https://login.microsoftonline.com/common/v2.0
medatarun.auth.jwt.trusted.issuer.azure.jwks=https://login.microsoftonline.com/common/discovery/v2.0/keys
```

:::warning
**Those values are examples**. Use the exact values provided by your identity provider.
:::

See [configuration reference](./configuration.md) for all available options.

Once configured, tokens can be used with:

- the [API](../usages/api-usage.mdx) via `Authorization: Bearer <token>`
- the [CLI](../usages/cli-usage.mdx) via environment variables
- for [your AI Agents configuration](../usages/mcp-usage.mdx)

```note
Tokens issued by Medatarun’s built-in identity provider are still accepted.
``` 

## Configure browser authentication (SSO)

This step controls how users authenticate when accessing Medatarun through a browser.

When using an external OIDC provider for browser authentication, the same provider must also be configured as a trusted JWT issuer.
Otherwise, users will be able to sign in but will not be authorized to perform API-backed actions.

In addition to configuring it as a trusted JWT issuer, you must provide:

- an OIDC authority URL (the issuer base URL used for OIDC discovery, not a specific endpoint),
- a `client_id` identifying your Medatarun installation as a registered application in that provider.

Medatarun automatically uses the standard OIDC discovery endpoint (`.well-known/openid-configuration`).

Example:

```properties
medatarun.auth.ui.oidc.authority=https://accounts.google.com
medatarun.auth.ui.oidc.clientid=medatarun
```
After this configuration, browser users authenticate through your identity provider.
The built-in Medatarun login page is no longer used.

This only affects browser authentication.

API and CLI access remain available with locally managed accounts and roles.