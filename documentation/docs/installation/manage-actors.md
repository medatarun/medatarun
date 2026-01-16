---
sidebar_position: 45
---

# Managing actors and roles

[Actor](./actors.md) commands apply to all identities known to Medatarun, whether they come from local users or external identity providers.

Using the CLI, API or UI you can run the following actions
assuming you are identified with an administrator role (see [how to manage admins here](./manage-users.md)).

## List actors

`medatarun auth actor_list`

List all known actors: all actors maintained by Medatarun and also all external actor that have connected at least once. Only available for admins.

This list gives you actor ids needed for further operations. 

## Disable or enable an actor 

`medatarun auth actor_disable --actorId=xxx`

Marks this actor disabled, meaning it won't be available to access Medatarun anymore.
Has no effect on already disabled actors.

Note that its account is not removed, just marked as `disabled`. You can re-enable it later if needed. 

`medatarun auth actor_enable --actorId=xxx`

Marks this actor enable. Has no effect on already enabled actors. 

## Roles 

You can set roles to actors, giving them permissions on the application. 

Currently, the only implemented role is `admin`.

We plan to have a fine grained list of roles later, for example, to distinguish model readers from authors.

## Relationship to users

User commands as seen in [Manage users](./manage-users.md) affect local users as well as their actor counterpart
in the same way. 

Actor commands affect authorization and access for all identities, local users and external users.

