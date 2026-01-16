---
sidebar_position: 35
---

# Identity and actors

Medatarun does not work directly with users or tokens.
All authenticated identities are represented internally as actors.

An actor is the internal identity used for authorization.
It is defined by the combination of:
- a token issuer,
- a subject identifier,
- and associated metadata (roles, status, display name).

Actors exist regardless of how authentication is performed.

An actor is uniquely identified by the combination of issuer and subject.

Actors can originate from:
- Medatarun’s built-in identity provider (local users),
- external identity providers (OIDC or other JWT issuers).

From Medatarun’s point of view, there is no functional difference between an internal or external actor once it exists.