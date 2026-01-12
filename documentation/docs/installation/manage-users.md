---
sidebar_position: 30
---

# Managing users

:::tip
Medatarun includes a minimal built-in authentication system to get started.
For production and large-scale deployments, see _Authentication model and scope_ at the end of this page.
:::

## Bootstrap and first admin user

When you launch Medatarun for the first time with `medatarun serve`, there are no users configured.

During startup, the backend generates a one-time bootstrap secret and prints it to the logs.

So, look at the starting logs. You will see a big message with a secret inside like this:

```
-----------------------------------------------------------
BOOTSTRAP SECRET (one-time usage): stCRYbQUCygJF6dNA_P-kuX2xKL3WfFMHMSe-jqvoIrOcnXOnb_Cy1PQXCPjNePt
Use it to create your admin account with CLI
... and instructions to create your admin account with CLI or API
-----------------------------------------------------------
```

This secret is used only once to create the initial administrator account.
It cannot be used to authenticate or perform API calls.

In another terminal, create the admin user (the password must be at least 14
characters and include uppercase, lowercase, numbers or special characters):

```bash
medatarun auth admin_bootstrap --username="admin" --fullname="Administrator" --password="admin.0123456789" --secret="copy the secret here"
```

Once this command succeeds, the admin account is created and the bootstrap
secret is permanently invalidated.

:::tip
If you came here from [Run Medatarun page](./run.mdx), this is enough to be able to sign in using the UI.

The rest of this document covers additional user and access management options.
:::

**Note**: for environments where you can not see logs, see the [configuration options](./configuration.md) page.
So, you can provide your own bootstrap secret (as environment variable, for example).

## Login as admin

Most CLI commands require you to be logged in.

To log in, run:

```bash
medatarun auth login --username="admin" --password="admin.0123456789"
```

The command returns an _access token_ (in `"access_token": "<the_token>"`). Copy this token and make it available to the
CLI by setting the `MEDATARUN_AUTH_TOKEN` environment variable:

```bash
export MEDATARUN_AUTH_TOKEN=<the_token>
```

If you have jq installed, you can do this in one step:

```bash
export MEDATARUN_AUTH_TOKEN=$(medatarun auth login --username="admin" --password="admin.0123456789" | jq -r '.access_token')
```

You can test that you are logged in:

```bash
medatarun auth whoami
```

To log out, close the terminal or unset the variable:

```bash
unset MEDATARUN_AUTH_TOKEN
```

## Users management (UI, CLI, APIs)

Please remember that everything you do in CLI can be done with API and UI.

UI provides a command panel where you can run those commands and a dynamic
interface that builds from all available commands. UI may be easier to use
since you don't have to fight with credentials.

## Create users

To create users, be sure to be logged in as admin.

```bash
medatarun auth user_create --username="john.doe" --fullname="John Doe" --password="john.doe.0123456789" --admin=false
```

## Change user password

### Admins can change a password

As logged as admin:

```bash
medatarun auth user_change_password --username="john.doe" --password="john.doe.0123456789"
```

### Feature to self-change their own password

Users can change their own password. We are aware that they don't use API to do that, 
but it provides a mechanism if you want to link another component to provide them
this feature. Provided commands are for CLI for readability, but you can use API with the same actions.

Current user must be logged in with their own token.

```bash
medatarun auth change_my_password --old_password="john.doe.0123456789" --new_password="john.doe.9876543210"
```

## Change user full name

Changes the full name of a user.  
The full name is used **only for display purposes**.

This feature exists because name corrections and spelling mistakes are common,
and being able to fix them matters to users.

```bash
medatarun auth user_change_fullname --username="john.doe" --fullname="John Doe"
```

## Authentication model and scope

Medatarun is designed to integrate naturally with a **full-featured OIDC identity provider**.

In a standard setup, authentication is delegated to an external IdP (such as Azure AD/Entra, Auth0, Keycloak, Google,
etc.),
and Medatarun relies exclusively on **JWT validation** to secure its API, CLI, MCP and user interface.

This model is the preferred and recommended approach for production deployments when you already have an identity
and security infrastructure.

At the same time, like many companies or individuals, you may not have an OpenID Connect identity provider — theory is one thing, practice is another.

You might be working in a small team, or alone, in an early-stage project,
in an isolated or air-gapped environment, or you may simply want to evaluate
Medatarun now without deploying and operating additional infrastructure.

For these situations, Medatarun provides a built-in authentication mechanism that allows you to start
and use the product immediately, without requiring an external dependency at first run.

This internal mechanism lets you:

- bootstrap the first administrator,
- issue and validate JWTs,
- manage a small set of local users.

It is intentionally simple and self-contained. Its role is to let you get started and operate Medatarun without
friction, not to replace a full identity management system.

When you later connect Medatarun to an external OIDC provider, nothing changes in the security model.
The API continues to validate JWTs in the same way; only the issuer of the tokens changes.

This way, you can start simple, and move to an enterprise-grade identity solution when it makes sense for you.