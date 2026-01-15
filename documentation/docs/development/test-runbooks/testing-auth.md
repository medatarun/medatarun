# Runbook - Testing Bootstrap

This doesn't replace unit test, just for end-to-end testing
and validating user scenarios.

Important: start with fresh install. Nothing in data

## Prerequisites

```bash
# make sure you have MEDATARUN_HOME set and matches the one of the server
echo $MEDATARUN_HOME
# if not set it
export MEDATARUN_HOME= # your install dir
# remove everything: database, secrets...
rm -rf ${MEDATARUN_HOME}/data
```

## Boostrap

Take then secret written in the starting logs.

```bash
# make sure you don't have token in memory
unset MEDATARUN_AUTH_TOKEN
# bootstrap admin
medatarun auth admin_bootstrap --username=admin --fullname="Administrator" --password="admin.0123456789" --secret=...
# login and get the token
export MEDATARUN_AUTH_TOKEN=$(medatarun auth login --username=admin --password="admin.0123456789" | jq -r '.access_token')
# try a whoami
medatarun auth whoami
```

Test: that you are logged in as admin with admin role.

## Bootstrap invalid secret

Remove everything from MEDATARUN_HOME (data and secrets).

```bash
rm -rf ${MEDATARUN_HOME}/data
unset MEDATARUN_AUTH_TOKEN
export MEDATARUN_AUTH_BOOTSTRAP_SECRET="012345678901234567890123456789"
```

In server logs, you should see this secret now

```bash
medatarun auth admin_bootstrap --username=admin --fullname="Administrator" --password="admin.0123456789" --secret="012345678901234567890123456789"
```

Should render

```text
2026-01-12 02:27:20,747 ERROR CLI - 500 - Bad bootstrap secret. {"details":"Bad bootstrap secret."}
```

Now try to login:

```bash
export MEDATARUN_AUTH_TOKEN=$(medatarun auth login --username=admin --password="admin.0123456789" | jq -r '.access_token')
```

Should render

```text
2026-01-12 02:28:10,664 ERROR CLI - 500 - Bad credentials. {"details":"Bad credentials."}
```

## Boostrap with provided secret

Remove everything from MEDATARUN_HOME (data and secrets).

```bash
rm -rf ${MEDATARUN_HOME}/data
unset MEDATARUN_AUTH_TOKEN
export MEDATARUN_AUTH_BOOTSTRAP_SECRET="012345678901234567890123456789"
```

In server logs, you should see this secret now

```bash
medatarun auth admin_bootstrap --username=admin --fullname="Administrator" --password="admin.0123456789" --secret="012345678901234567890123456789"
export MEDATARUN_AUTH_TOKEN=$(medatarun auth login --username=admin --password="admin.0123456789" | jq -r '.access_token')
```

## Boostrap consumed

Prerequisites: boostrap secret shall be used (secret is ok but password is new)

```bash
medatarun auth admin_bootstrap --username=admin --fullname="Administrator" --password="admin.9876543210" --secret="012345678901234567890123456789"
```

You should have

```text
2026-01-12 02:23:30,274 ERROR CLI - 500 - Bootstrap already consumed. {"details":"Bootstrap already consumed."}
```

Verify nothing changed (and that password had not been changed)

```text
export MEDATARUN_AUTH_TOKEN=$(medatarun auth login --username=admin --password="admin.0123456789" | jq -r '.access_token')
medatarun auth whoami
```

## Create user and sign-in with him

```bash
# prepare
export MEDATARUN_AUTH_BOOTSTRAP_SECRET="012345678901234567890123456789"
rm -rf ${MEDATARUN_HOME}/data; medatarun serve
unset MEDATARUN_AUTH_TOKEN
# Boostrap and create admin
medatarun auth admin_bootstrap --username=admin --fullname="Administrator" --password="admin.0123456789" --secret="${MEDATARUN_AUTH_BOOTSTRAP_SECRET}"
export MEDATARUN_AUTH_TOKEN=$(medatarun auth login --username=admin --password="admin.0123456789" | jq -r '.access_token')
medatarun auth whoami
# Created John Doe
medatarun auth user_create --username="john.doe" --password="john.doe.0123456789" --fullname="John Doe" --admin=false
unset MEDATARUN_AUTH_TOKEN; export MEDATARUN_AUTH_TOKEN=$(medatarun auth login --username=admin --password="admin.0123456789" | jq -r '.access_token')
medatarun auth whoami
# Created users must appear as actors
medatarun auth list_actors | jq
```