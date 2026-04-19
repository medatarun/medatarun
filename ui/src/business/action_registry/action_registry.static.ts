import type { ActionRegistryDto } from "@/business/action_registry/action_registry.dto.ts";

export const actionRegistryStatic = {
  items: [
    {
      id: "019da648-2a13-7ad9-9c09-42a745c7d582",
      groupKey: "batch",
      actionKey: "batch_run",
      title: "Batch commands",
      description: "Process a list of commands all at once",
      parameters: [
        {
          name: "actions",
          type: "List<ActionWithPayload>",
          jsonType: "array",
          optional: false,
          title: "Actions",
          description:
            "List of actions to run in batch. You should provide for each action `group`, `action`, and `payload`. Payload is required even if empty.",
          order: 0,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a1d-75be-96df-fb3d6530d678",
      groupKey: "auth",
      actionKey: "actor_disable",
      title: "Disable actor",
      description: "Disable an actor. Only available for admins.",
      parameters: [
        {
          name: "actorId",
          type: "ActorId",
          jsonType: "string",
          optional: false,
          title: "actorId",
          description: "Actor identifier",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "actor",
            referencingParams: [
              {
                name: "actorId",
                kind: "id",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a1f-74fd-9e32-a585b94e0a2d",
      groupKey: "auth",
      actionKey: "actor_enable",
      title: "Enable actor",
      description: "Enable an actor. Only available for admins.",
      parameters: [
        {
          name: "actorId",
          type: "ActorId",
          jsonType: "string",
          optional: false,
          title: "actorId",
          description: "Actor identifier",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "actor",
            referencingParams: [
              {
                name: "actorId",
                kind: "id",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a20-79db-9afb-bce43d86e009",
      groupKey: "auth",
      actionKey: "actor_get",
      title: "Get actor",
      description: "Get an actor by identifier. Only available for admins.",
      parameters: [
        {
          name: "actorId",
          type: "ActorId",
          jsonType: "string",
          optional: false,
          title: "actorId",
          description: "Actor identifier",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["actor"],
      },
    },
    {
      id: "019da648-2a22-7a4d-9d9f-7aba0002250e",
      groupKey: "auth",
      actionKey: "actor_list",
      title: "List actors",
      description:
        "List all known actors: all actors maintained by Medatarun and also all external actor that have connected at least once. Only available for admins.",
      parameters: [],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["actor"],
      },
    },
    {
      id: "019da648-2a23-774b-baf3-e9c00ebb3f79",
      groupKey: "auth",
      actionKey: "actor_add_role",
      title: "Add actor role",
      description: "Add role to actor.",
      parameters: [
        {
          name: "actorId",
          type: "ActorId",
          jsonType: "string",
          optional: false,
          title: "actorId",
          description: "Actor identifier",
          order: 1,
        },
        {
          name: "roleRef",
          type: "RoleRef",
          jsonType: "string",
          optional: false,
          title: "roleRef",
          description: "Role to add",
          order: 2,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "actor",
            referencingParams: [
              {
                name: "actorId",
                kind: "id",
              },
              {
                name: "roleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a24-7476-a90b-151525bf2fd3",
      groupKey: "auth",
      actionKey: "actor_delete_role",
      title: "Delete actor role",
      description: "Delete role from actor.",
      parameters: [
        {
          name: "actorId",
          type: "ActorId",
          jsonType: "string",
          optional: false,
          title: "actorId",
          description: "Actor identifier",
          order: 1,
        },
        {
          name: "roleRef",
          type: "RoleRef",
          jsonType: "string",
          optional: false,
          title: "roleRef",
          description: "Role to delete",
          order: 2,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "actor",
            referencingParams: [
              {
                name: "actorId",
                kind: "id",
              },
              {
                name: "roleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a24-7df3-a90c-b9ed7fbf0bbb",
      groupKey: "auth",
      actionKey: "admin_bootstrap",
      title: "Creates admin user",
      description:
        "Creates admin user account and bootstrap credentials. Consumes the one-time secret generated at install. This will automatically make the admin available as an actor and able to connect with tokens.",
      parameters: [
        {
          name: "fullname",
          type: "Fullname",
          jsonType: "string",
          optional: false,
          title: "fullname",
          description: "User full name (displayed name)",
          order: 3,
        },
        {
          name: "password",
          type: "PasswordClear",
          jsonType: "string",
          optional: false,
          title: "password",
          description: "Admin password",
          order: 4,
        },
        {
          name: "secret",
          type: "String",
          jsonType: "string",
          optional: false,
          title: "secret",
          description: "Secret provided at bootstrap",
          order: 5,
        },
        {
          name: "username",
          type: "Username",
          jsonType: "string",
          optional: false,
          title: "username",
          description: "Admin user name",
          order: 2,
        },
      ],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a26-79ba-8bd3-7ca8ce76841a",
      groupKey: "auth",
      actionKey: "change_my_password",
      title: "Change own password",
      description:
        "Change connected user password. Must provide current password and a new password. Only available to authentified user.",
      parameters: [
        {
          name: "newPassword",
          type: "PasswordClear",
          jsonType: "string",
          optional: false,
          title: "newPassword",
          description: "New Password",
          order: 2,
        },
        {
          name: "oldPassword",
          type: "PasswordClear",
          jsonType: "string",
          optional: false,
          title: "currentPassword",
          description: "Current Password",
          order: 1,
        },
      ],
      securityRule: "signed_in",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a27-7456-812d-a7fa8380a853",
      groupKey: "auth",
      actionKey: "login",
      title: "Login user",
      description:
        "Generates a JWT Access Token for API calls that users can reuse to authenticate themselves in API or CLI calls (OAuth format, not OIDC)",
      parameters: [
        {
          name: "password",
          type: "PasswordClear",
          jsonType: "string",
          optional: false,
          title: "password",
          description: "Password",
          order: 2,
        },
        {
          name: "username",
          type: "Username",
          jsonType: "string",
          optional: false,
          title: "username",
          description: "User name",
          order: 1,
        },
      ],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a27-7f64-812e-9d2a56222afc",
      groupKey: "auth",
      actionKey: "role_add_permission",
      title: "Add role permission",
      description: "Add a permission to a role.",
      parameters: [
        {
          name: "permissionKey",
          type: "PermissionKey",
          jsonType: "string",
          optional: false,
          title: "Permission",
          description: "Permission key",
          order: 2,
        },
        {
          name: "roleRef",
          type: "RoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role reference",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "role",
            referencingParams: [
              {
                name: "permissionKey",
                kind: "key",
              },
              {
                name: "roleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a29-75ce-b72f-48e8972862f1",
      groupKey: "auth",
      actionKey: "role_create",
      title: "Create role",
      description: "Create a new role.",
      parameters: [
        {
          name: "description",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Role description, explain what this role is for and when it can be used.",
          order: 3,
        },
        {
          name: "key",
          type: "RoleKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Role key. Must be unique across all roles. Serves as a technical identifier for integrations.",
          order: 2,
        },
        {
          name: "name",
          type: "String",
          jsonType: "string",
          optional: false,
          title: "Name",
          description: "Role display name.",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "role",
            referencingParams: [
              {
                name: "key",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a2a-785a-8bae-a9d5e75a6492",
      groupKey: "auth",
      actionKey: "role_delete",
      title: "Delete role",
      description: "Delete a role.",
      parameters: [
        {
          name: "roleRef",
          type: "RoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role reference",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "role",
            referencingParams: [
              {
                name: "roleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a2b-770a-b702-44508807b49f",
      groupKey: "auth",
      actionKey: "role_delete_permission",
      title: "Delete role permission",
      description: "Delete a permission from a role.",
      parameters: [
        {
          name: "permissionKey",
          type: "PermissionKey",
          jsonType: "string",
          optional: false,
          title: "Permission",
          description: "Permission key",
          order: 2,
        },
        {
          name: "roleRef",
          type: "RoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role reference",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "role",
            referencingParams: [
              {
                name: "permissionKey",
                kind: "key",
              },
              {
                name: "roleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a2c-7249-b451-0ccdbefbb73f",
      groupKey: "auth",
      actionKey: "role_get",
      title: "Get role",
      description: "Get a role and its permissions.",
      parameters: [
        {
          name: "roleRef",
          type: "RoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role reference",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["role"],
      },
    },
    {
      id: "019da648-2a2c-7f06-b452-b12e15d16e64",
      groupKey: "auth",
      actionKey: "role_list",
      title: "List roles",
      description: "List all roles.",
      parameters: [],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["role"],
      },
    },
    {
      id: "019da648-2a2d-77a9-8976-5ea72e52acba",
      groupKey: "auth",
      actionKey: "role_update_description",
      title: "Update role description",
      description: "Update a role description.",
      parameters: [
        {
          name: "roleRef",
          type: "RoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role reference",
          order: 1,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Description",
          description: "Role description",
          order: 2,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "role",
            referencingParams: [
              {
                name: "roleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a2e-75b2-9848-3816254529c3",
      groupKey: "auth",
      actionKey: "role_update_key",
      title: "Update role key",
      description: "Update a role key.",
      parameters: [
        {
          name: "roleRef",
          type: "RoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role reference",
          order: 1,
        },
        {
          name: "value",
          type: "RoleKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description: "Role key",
          order: 2,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "role",
            referencingParams: [
              {
                name: "roleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a2e-7f58-9849-a38647051a87",
      groupKey: "auth",
      actionKey: "role_update_name",
      title: "Update role name",
      description: "Update a role name.",
      parameters: [
        {
          name: "roleRef",
          type: "RoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role reference",
          order: 1,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: false,
          title: "Name",
          description: "Role name",
          order: 2,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "role",
            referencingParams: [
              {
                name: "roleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a2f-7ed0-9694-b71b51fbf54f",
      groupKey: "auth",
      actionKey: "user_change_fullname",
      title: "Change user full name",
      description:
        "Change user full name. Only available for admins. This will automatically change the corresponding actor fullname.",
      parameters: [
        {
          name: "fullname",
          type: "Fullname",
          jsonType: "string",
          optional: false,
          title: "fullname",
          description: "Full name (displayed name)",
          order: 2,
        },
        {
          name: "username",
          type: "Username",
          jsonType: "string",
          optional: false,
          title: "username",
          description: "User name",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a30-783d-9202-f673dc87ac8b",
      groupKey: "auth",
      actionKey: "user_change_password",
      title: "Change user password",
      description: "Change a user password. Only available for admins.",
      parameters: [
        {
          name: "password",
          type: "PasswordClear",
          jsonType: "string",
          optional: false,
          title: "password",
          description: "New password for this user",
          order: 2,
        },
        {
          name: "username",
          type: "Username",
          jsonType: "string",
          optional: false,
          title: "username",
          description: "User name",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a31-7381-bdc1-62a865fc3ea9",
      groupKey: "auth",
      actionKey: "user_create",
      title: "Create user",
      description:
        "Create a new user. This will automatically make this user available as an actor and able to connect with security tokens.",
      parameters: [
        {
          name: "admin",
          type: "Boolean",
          jsonType: "boolean",
          optional: false,
          title: "Administrator privileges",
          description:
            "Gives this user or tool administrator privileges. This gives or removes the special admin role in the corresponding actor.",
          order: 4,
        },
        {
          name: "fullname",
          type: "Fullname",
          jsonType: "string",
          optional: false,
          title: "Displayed name",
          description:
            "How this user or tool is named on the screens. Usually the first and last name of a person, or the full name of the tool.",
          order: 2,
        },
        {
          name: "password",
          type: "PasswordClear",
          jsonType: "string",
          optional: false,
          title: "password",
          description: "User password",
          order: 3,
        },
        {
          name: "username",
          type: "Username",
          jsonType: "string",
          optional: false,
          title: "User or tool name (login)",
          description:
            "User name or tool name used on the login page or to get security tokens. Avoid special characters.",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "user",
            referencingParams: [],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a32-71b2-b706-93ec1c614072",
      groupKey: "auth",
      actionKey: "user_disable",
      title: "Disable user",
      description:
        "Disable a user account. Only available for admins. This will automatically make the corresponding actor disabled and unable to connect with tokens.",
      parameters: [
        {
          name: "username",
          type: "Username",
          jsonType: "string",
          optional: false,
          title: "username",
          description: "User name",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a32-7b64-b707-7270ee6e749b",
      groupKey: "auth",
      actionKey: "user_enable",
      title: "Enable user",
      description:
        "Enable a user account. Only available for admins. This will automatically make the corresponding actor enabled and able to connect with tokens.",
      parameters: [
        {
          name: "username",
          type: "Username",
          jsonType: "string",
          optional: false,
          title: "username",
          description: "User name",
          order: 1,
        },
      ],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a33-7ab8-81df-39356373bfce",
      groupKey: "auth",
      actionKey: "user_list",
      title: "User list",
      description: "Lists available users. Only available for admins.",
      parameters: [],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["user"],
      },
    },
    {
      id: "019da648-2a34-7abc-b8ea-40e40b876235",
      groupKey: "auth",
      actionKey: "whoami",
      title: "Who am i",
      description:
        "Tells who is the connected user. Allow you to know if you have the credentials you need",
      parameters: [],
      securityRule: "signed_in",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a36-73a9-91d9-f0aafa427ef4",
      groupKey: "tag",
      actionKey: "maintenance_rebuild_caches",
      title: "Maintenance rebuild caches",
      description:
        "\n            Rebuilds tag application caches from stored events.\n            \n            Use this only as an exceptional maintenance action when data appears out of date.\n            If you need to run it, we recommend contacting us on the project GitHub because it\n            usually means you identified a bug.\n        ",
      parameters: [],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a36-7876-91da-7f5528ac8684",
      groupKey: "tag",
      actionKey: "tag_global_create",
      title: "Create a global tag",
      description: "Creates a tag inside a group.",
      parameters: [
        {
          name: "description",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Explain what this tag means and when it should be used.",
          order: 40,
        },
        {
          name: "groupRef",
          type: "TagGroupRef",
          jsonType: "string",
          optional: false,
          title: "Group",
          description: "Group that will contain this tag.",
          order: 10,
        },
        {
          name: "key",
          type: "TagKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Stable business code used to identify this tag inside its group.\n\nUse only letters, digits, `_` and `-`.",
          order: 30,
        },
        {
          name: "name",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this tag.",
          order: 20,
        },
      ],
      securityRule: "tag_global_manage",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "tag_global",
            referencingParams: [
              {
                name: "groupRef",
                kind: "ref",
              },
              {
                name: "key",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a38-7008-8498-f92e48efed46",
      groupKey: "tag",
      actionKey: "tag_global_delete",
      title: "Delete global tag",
      description: "Deletes a global tag.",
      parameters: [
        {
          name: "tagRef",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to delete.",
          order: 10,
        },
      ],
      securityRule: "tag_global_manage",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "tag_global",
            referencingParams: [
              {
                name: "tagRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a38-77c6-8499-e517bd32ba9e",
      groupKey: "tag",
      actionKey: "tag_global_update_description",
      title: "Update global tag description",
      description: "Updates the description of a global tag.",
      parameters: [
        {
          name: "tagRef",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to update.",
          order: 10,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Explain what this tag means and when it should be used.",
          order: 20,
        },
      ],
      securityRule: "tag_global_manage",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "tag_global",
            referencingParams: [
              {
                name: "tagRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a38-7f22-849a-c0afb5ec519b",
      groupKey: "tag",
      actionKey: "tag_global_update_key",
      title: "Update global tag key",
      description: "Updates the key of a global tag.",
      parameters: [
        {
          name: "tagRef",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to update.",
          order: 10,
        },
        {
          name: "value",
          type: "TagKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Stable business code used to identify this tag inside its group.\n\nUse only letters, digits, `_` and `-`.",
          order: 20,
        },
      ],
      securityRule: "tag_global_manage",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "tag_global",
            referencingParams: [
              {
                name: "tagRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a39-7be3-967a-862c144965d5",
      groupKey: "tag",
      actionKey: "tag_global_update_name",
      title: "Update global tag name",
      description: "Updates the name of a global tag.",
      parameters: [
        {
          name: "tagRef",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to update.",
          order: 10,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: false,
          title: "Name",
          description: "Name of this tag.",
          order: 20,
        },
      ],
      securityRule: "tag_global_manage",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "tag_global",
            referencingParams: [
              {
                name: "tagRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a3a-7791-ae4b-d6b00a936319",
      groupKey: "tag",
      actionKey: "tag_group_create",
      title: "Create a tag group",
      description:
        "Creates a group used to organize global tags that belong together.",
      parameters: [
        {
          name: "description",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Explain what this group is for and which tags belong in it.",
          order: 40,
        },
        {
          name: "key",
          type: "TagGroupKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Stable business code used to identify this group.\n\nIt must be unique across all groups.",
          order: 30,
        },
        {
          name: "name",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this group.",
          order: 20,
        },
      ],
      securityRule: "tag_group_manage",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "tag_group",
            referencingParams: [
              {
                name: "key",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a3b-7147-a01c-028f76925974",
      groupKey: "tag",
      actionKey: "tag_group_delete",
      title: "Delete tag group",
      description: "Deletes a tag group.",
      parameters: [
        {
          name: "tagGroupRef",
          type: "TagGroupRef",
          jsonType: "string",
          optional: false,
          title: "Tag group",
          description: "Group to delete.",
          order: 10,
        },
      ],
      securityRule: "tag_group_manage",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "tag_group",
            referencingParams: [
              {
                name: "tagGroupRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a3b-766a-a01d-af6f8ac11f0d",
      groupKey: "tag",
      actionKey: "tag_group_list",
      title: "Tag group list",
      description: "Lists all tag groups.",
      parameters: [],
      securityRule: "signed_in",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["tag_group"],
      },
    },
    {
      id: "019da648-2a3b-7985-a01e-727ab60fa9e1",
      groupKey: "tag",
      actionKey: "tag_group_update_description",
      title: "Update tag group description",
      description: "Updates the description of a tag group.",
      parameters: [
        {
          name: "tagGroupRef",
          type: "TagGroupRef",
          jsonType: "string",
          optional: false,
          title: "Tag group",
          description: "Group to update.",
          order: 10,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Explain what this group is for and which tags belong in it.",
          order: 20,
        },
      ],
      securityRule: "tag_group_manage",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "tag_group",
            referencingParams: [
              {
                name: "tagGroupRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a3b-7f33-a01f-5af3cb8ceb35",
      groupKey: "tag",
      actionKey: "tag_group_update_key",
      title: "Update tag group key",
      description: "Updates the key of a tag group.",
      parameters: [
        {
          name: "tagGroupRef",
          type: "TagGroupRef",
          jsonType: "string",
          optional: false,
          title: "Tag group",
          description: "Group to update.",
          order: 10,
        },
        {
          name: "value",
          type: "TagGroupKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Stable business code used to identify this group.\n\nIt must be unique across all groups.",
          order: 20,
        },
      ],
      securityRule: "tag_group_manage",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "tag_group",
            referencingParams: [
              {
                name: "tagGroupRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a3c-76fd-a349-f479aff5f495",
      groupKey: "tag",
      actionKey: "tag_group_update_name",
      title: "Update global tag group name",
      description: "Updates the name of a tag group.",
      parameters: [
        {
          name: "tagGroupRef",
          type: "TagGroupRef",
          jsonType: "string",
          optional: false,
          title: "Tag group",
          description: "Group to update.",
          order: 10,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: false,
          title: "Name",
          description: "Name of this group.",
          order: 20,
        },
      ],
      securityRule: "tag_group_manage",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "tag_group",
            referencingParams: [
              {
                name: "tagGroupRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a3d-703d-94b1-bb5e9bf03bda",
      groupKey: "tag",
      actionKey: "tag_local_create",
      title: "Create a local tag",
      description:
        "Creates a tag, local to a scope (a model for example), without belonging to a group.",
      parameters: [
        {
          name: "description",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Optional help text shown to users.\n\nExplain what the tag means and when it should be used.",
          order: 40,
        },
        {
          name: "key",
          type: "TagKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Stable business code used to identify this tag in this scope.\n\nUse only letters, digits, `_` and `-`.\n\nExample: `customer-visible`",
          order: 30,
        },
        {
          name: "name",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this tag.",
          order: 20,
        },
        {
          name: "scopeRef",
          type: "TagScopeRef",
          jsonType: "object",
          optional: false,
          title: "Scope",
          description: "Scope where this tag will be available.",
          order: 10,
        },
      ],
      securityRule: "tag_local_manage",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "tag_local",
            referencingParams: [
              {
                name: "key",
                kind: "key",
              },
              {
                name: "scopeRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a3f-71e3-b4f8-4f51d1710965",
      groupKey: "tag",
      actionKey: "tag_local_delete",
      title: "Delete local tag",
      description: "Deletes a local tag from its scope.",
      parameters: [
        {
          name: "tagRef",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to delete.",
          order: 10,
        },
      ],
      securityRule: "tag_local_manage",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "tag_local",
            referencingParams: [
              {
                name: "tagRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a3f-7b3f-b4f9-8311bb45d3fb",
      groupKey: "tag",
      actionKey: "tag_local_update_description",
      title: "Update local tag description",
      description: "Updates the description of a local tag.",
      parameters: [
        {
          name: "tagRef",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to update.",
          order: 10,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Explain what this tag means and when it should be used.",
          order: 20,
        },
      ],
      securityRule: "tag_local_manage",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "tag_local",
            referencingParams: [
              {
                name: "tagRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a40-7497-8536-3414e4fe25cd",
      groupKey: "tag",
      actionKey: "tag_local_update_key",
      title: "Update local tag key",
      description: "Updates the key of a local tag.",
      parameters: [
        {
          name: "tagRef",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to update.",
          order: 10,
        },
        {
          name: "value",
          type: "TagKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Stable business code used to identify this tag in its scope.\n\nUse only letters, digits, `_` and `-`.",
          order: 20,
        },
      ],
      securityRule: "tag_local_manage",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "tag_local",
            referencingParams: [
              {
                name: "tagRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a40-7add-8537-37c39cf84e93",
      groupKey: "tag",
      actionKey: "tag_local_update_name",
      title: "Update local tag name",
      description: "Updates the name of a local tag.",
      parameters: [
        {
          name: "tagRef",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to update.",
          order: 10,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this tag.",
          order: 20,
        },
      ],
      securityRule: "tag_local_manage",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "tag_local",
            referencingParams: [
              {
                name: "tagRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a41-70f9-bcfc-472954dc312a",
      groupKey: "tag",
      actionKey: "tag_search",
      title: "Tag search",
      description:
        "Searches known tags. Without filters, returns all tags. Use filters to narrow the result.",
      parameters: [
        {
          name: "filters",
          type: "TagSearchFilters",
          jsonType: "object",
          optional: true,
          title: "Filters",
          description: "Optional filters used to narrow the search result.",
          order: 10,
        },
      ],
      securityRule: "signed_in",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["tag"],
      },
    },
    {
      id: "019da648-2a46-7ddf-a766-2872b015a6e8",
      groupKey: "model",
      actionKey: "business_key_create",
      title: "Create a business key",
      description:
        "Creates a business key to represent wich attributes of an entity uniquely identifies the objet in a business manner.",
      parameters: [
        {
          name: "description",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description: "Business key description",
          order: 14,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this business key is located.",
          order: 20,
        },
        {
          name: "key",
          type: "BusinessKeyKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description: "Business key's own key",
          order: 12,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity containing this business key is located.",
          order: 10,
        },
        {
          name: "name",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Business key name",
          order: 11,
        },
        {
          name: "participants",
          type: "List<EntityAttributeRef>",
          jsonType: "array",
          optional: false,
          title: "Participants",
          description:
            "List of attributes that participate in the business key, in order.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "business_key",
            referencingParams: [
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "key",
                kind: "key",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a49-779d-b143-01fc7ac81641",
      groupKey: "model",
      actionKey: "business_key_delete",
      title: "Delete business key",
      description: "Delete the business key. Attributes are kept in entity.",
      parameters: [
        {
          name: "businessKeyRef",
          type: "BusinessKeyRef",
          jsonType: "string",
          optional: false,
          title: "Business key",
          description: "Business key to delete.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where the business key is located.",
          order: 10,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "business_key",
            referencingParams: [
              {
                name: "businessKeyRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a4a-759d-b8d1-cc2db5c669f6",
      groupKey: "model",
      actionKey: "business_key_update_description",
      title: "Update business key description",
      description: "Changes the description of a business key.",
      parameters: [
        {
          name: "businessKeyRef",
          type: "BusinessKeyRef",
          jsonType: "string",
          optional: false,
          title: "Business key",
          description: "Business key to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where the business key is located.",
          order: 10,
        },
        {
          name: "value",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "New name",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "business_key",
            referencingParams: [
              {
                name: "businessKeyRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a4c-74c4-93d2-23dcf785a51c",
      groupKey: "model",
      actionKey: "business_key_update_key",
      title: "Update business key's key",
      description: "Changes the key of a business key.",
      parameters: [
        {
          name: "businessKeyRef",
          type: "BusinessKeyRef",
          jsonType: "string",
          optional: false,
          title: "Business key",
          description: "Business key to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where the business key is located.",
          order: 10,
        },
        {
          name: "value",
          type: "BusinessKeyKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description: "New key",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "business_key",
            referencingParams: [
              {
                name: "businessKeyRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a4d-73f7-bbe2-2c74ba51e5af",
      groupKey: "model",
      actionKey: "business_key_update_name",
      title: "Update business key name",
      description: "Changes the name of a business key.",
      parameters: [
        {
          name: "businessKeyRef",
          type: "BusinessKeyRef",
          jsonType: "string",
          optional: false,
          title: "Business key",
          description: "Business key to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where the business key is located.",
          order: 10,
        },
        {
          name: "value",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "New name",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "business_key",
            referencingParams: [
              {
                name: "businessKeyRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a4e-721c-abd1-b83b590f4830",
      groupKey: "model",
      actionKey: "business_key_update_participants",
      title: "Update business key participants",
      description:
        "Changes the participants of a business key, meaning all attributes that define the business key meaning.",
      parameters: [
        {
          name: "businessKeyRef",
          type: "BusinessKeyRef",
          jsonType: "string",
          optional: false,
          title: "Business key",
          description: "Business key to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where the business key is located.",
          order: 10,
        },
        {
          name: "value",
          type: "List<EntityAttributeRef>",
          jsonType: "array",
          optional: false,
          title: "Participants",
          description:
            "List of the entity attributes that define the business key, in order.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "business_key",
            referencingParams: [
              {
                name: "businessKeyRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a4e-7ffb-abd2-84521e40ba28",
      groupKey: "model",
      actionKey: "model_compare",
      title: "Compare models",
      description: "Compares two model states and returns their differences.",
      parameters: [
        {
          name: "leftModelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Left model",
          description: "Left model to compare.",
          order: 10,
        },
        {
          name: "leftModelVersion",
          type: "ModelVersion",
          jsonType: "string",
          optional: true,
          title: "Left version",
          description:
            "Version of the left model to compare. If not specified, the current state is used.",
          order: 20,
        },
        {
          name: "rightModelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Right model",
          description: "Right model to compare.",
          order: 30,
        },
        {
          name: "rightModelVersion",
          type: "ModelVersion",
          jsonType: "string",
          optional: true,
          title: "Right version",
          description:
            "Version of the right model to compare. If not specified, the current state is used.",
          order: 40,
        },
        {
          name: "scope",
          type: "ModelDiffScope",
          jsonType: "string",
          optional: false,
          title: "Comparison scope",
          description:
            "Choose whether to compare only the structure of the two models, or the structure together with their names, descriptions, and other texts.",
          order: 50,
        },
      ],
      securityRule: "model_read",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a50-7bc2-a437-5bf5450df79c",
      groupKey: "model",
      actionKey: "entity_attribute_add_tag",
      title: "Add tag to entity attribute",
      description: "Adds a tag to an entity attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "EntityAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this attribute is located.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity containing this attribute is located.",
          order: 10,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to add to this attribute.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a51-7b74-aabf-c9ba4e4216cd",
      groupKey: "model",
      actionKey: "entity_attribute_create",
      title: "Create entity attribute",
      description: "Creates an attribute on an entity.",
      parameters: [
        {
          name: "attributeKey",
          type: "AttributeKey",
          jsonType: "string",
          optional: false,
          title: "Attribute key",
          description:
            "Provide a stable code for this attribute. This code is used to identify it uniquely in the entity. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 40,
        },
        {
          name: "description",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of what this attribute represents, what information it contains, which values are expected, the main rules that apply to it, and any useful examples or notes.",
          order: 70,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this attribute will be created.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity that will contain attribute is located.",
          order: 10,
        },
        {
          name: "name",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this attribute.",
          order: 30,
        },
        {
          name: "optional",
          type: "Boolean",
          jsonType: "boolean",
          optional: false,
          title: "Optional",
          description:
            "Choose whether this attribute is required for all occurrences of the entity, or optional for some of them.",
          order: 60,
        },
        {
          name: "type",
          type: "TypeRef",
          jsonType: "string",
          optional: false,
          title: "Data type",
          description:
            "Choose the data type of the information carried by this attribute.",
          order: 50,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "entity_attribute",
            referencingParams: [
              {
                name: "attributeKey",
                kind: "key",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a53-7a66-80a0-075c167888a5",
      groupKey: "model",
      actionKey: "entity_attribute_delete",
      title: "Delete entity attribute",
      description: "Deletes an attribute from an entity.",
      parameters: [
        {
          name: "attributeRef",
          type: "EntityAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to delete.",
          order: 30,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this attribute is located.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity containing this attribute is located.",
          order: 10,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "entity_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a54-7666-bf10-e2e99c8dd9ce",
      groupKey: "model",
      actionKey: "entity_attribute_delete_tag",
      title: "Delete tag from entity attribute",
      description: "Removes a tag from an entity attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "EntityAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this attribute is located.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity containing this attribute is located.",
          order: 10,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to remove from this attribute.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a56-7ea3-9152-47d69e21821e",
      groupKey: "model",
      actionKey: "entity_attribute_update_description",
      title: "Update entity attribute description",
      description: "Updates the description of an entity attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "EntityAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this attribute is located.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity containing this attribute is located.",
          order: 10,
        },
        {
          name: "value",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of what this attribute represents, what information it contains, which values are expected, the main rules that apply to it, and any useful examples or notes.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a58-7543-8db6-6293f21862a0",
      groupKey: "model",
      actionKey: "entity_attribute_update_key",
      title: "Update entity attribute key",
      description: "Updates the key of an entity attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "EntityAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this attribute is located.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity containing this attribute is located.",
          order: 10,
        },
        {
          name: "value",
          type: "AttributeKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide the stable code used to identify this attribute. It must be unique in the entity. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a59-7574-b241-fbd10340bb0d",
      groupKey: "model",
      actionKey: "entity_attribute_update_name",
      title: "Update entity attribute name",
      description: "Updates the name of an entity attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "EntityAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this attribute is located.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity containing this attribute is located.",
          order: 10,
        },
        {
          name: "value",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this attribute.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a5a-7d33-8231-c0d4fb6158b4",
      groupKey: "model",
      actionKey: "entity_attribute_update_optional",
      title: "Update entity attribute optionality",
      description: "Updates whether an entity attribute is optional.",
      parameters: [
        {
          name: "attributeRef",
          type: "EntityAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this attribute is located.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity containing this attribute is located.",
          order: 10,
        },
        {
          name: "value",
          type: "Boolean",
          jsonType: "boolean",
          optional: false,
          title: "Optional",
          description:
            "Choose whether this attribute is required for all occurrences of the entity, or optional for some of them.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a5b-77db-b91d-97d3b4bc704f",
      groupKey: "model",
      actionKey: "entity_attribute_update_type",
      title: "Update entity attribute type",
      description: "Updates the data type of an entity attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "EntityAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity where this attribute is located.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description:
            "Model where the entity containing this attribute is located.",
          order: 10,
        },
        {
          name: "value",
          type: "TypeRef",
          jsonType: "string",
          optional: false,
          title: "Data type",
          description:
            "Choose the data type of the information carried by this attribute.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a5c-7241-9ad5-2e89ab6489a3",
      groupKey: "model",
      actionKey: "entity_primary_key_update",
      title: "Update entity primary key",
      description:
        "Defines the primary key of an entity. Note that if the list of attributes is empty, the primary key will be removed.",
      parameters: [
        {
          name: "attributeRef",
          type: "List<EntityAttributeRef>",
          jsonType: "array",
          optional: false,
          title: "Attributes",
          description:
            "Attributes that participate in the primary key in order.",
          order: 30,
        },
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity for which we set the primary key.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where the entity is located.",
          order: 10,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a5c-7e8f-9ad6-7341b202d713",
      groupKey: "model",
      actionKey: "entity_add_tag",
      title: "Add entity tag",
      description: "Adds a tag to an entity.",
      parameters: [
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this entity is located.",
          order: 10,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to add to this entity.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity",
            referencingParams: [
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a5d-7631-aa0c-353a3b6d4fdd",
      groupKey: "model",
      actionKey: "entity_create",
      title: "Create entity",
      description: "Creates an entity in an existing model.",
      parameters: [
        {
          name: "description",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of what this entity represents in the domain. Explain the business concept behind it, what belongs in it, the main rules that apply to it, how it should be used, etc.",
          order: 40,
        },
        {
          name: "documentationHome",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "External documentation",
          description: "Link to external documentation for this entity.",
          order: 80,
        },
        {
          name: "entityKey",
          type: "EntityKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide a stable code for this entity. This code is used to identify it uniquely in the model. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 30,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this entity will be created.",
          order: 10,
        },
        {
          name: "name",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this entity.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "entity",
            referencingParams: [
              {
                name: "entityKey",
                kind: "key",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a5e-7912-90ef-b96e7bab1d7f",
      groupKey: "model",
      actionKey: "entity_delete",
      title: "Delete model entity",
      description:
        "Removes an entity and all its attributes from the given model.",
      parameters: [
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity to delete.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this entity is located.",
          order: 10,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "entity",
            referencingParams: [
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a5f-7224-8d81-1159fb6ccdd4",
      groupKey: "model",
      actionKey: "entity_delete_tag",
      title: "Delete entity tag",
      description: "Removes a tag from an entity.",
      parameters: [
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this entity is located.",
          order: 10,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to remove from this entity.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity",
            referencingParams: [
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a5f-79f3-8d82-8f28cac45af8",
      groupKey: "model",
      actionKey: "entity_update_description",
      title: "Update entity description",
      description: "Updates the description of an entity.",
      parameters: [
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this entity is located.",
          order: 10,
        },
        {
          name: "value",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of what this entity represents in the domain. Explain the business concept behind it, what belongs in it, the main rules that apply to it, how it should be used, and any other information.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity",
            referencingParams: [
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a60-7235-bced-88a9f72105ba",
      groupKey: "model",
      actionKey: "entity_update_documentation_link",
      title: "Update entity external documentation",
      description: "Updates the external documentation link of an entity.",
      parameters: [
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this entity is located.",
          order: 10,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "URL",
          description: "Link to external documentation for this entity.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity",
            referencingParams: [
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a60-7a1c-bcee-05d3b887f4a3",
      groupKey: "model",
      actionKey: "entity_update_key",
      title: "Update entity key",
      description: "Updates the key of an entity.",
      parameters: [
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this entity is located.",
          order: 10,
        },
        {
          name: "value",
          type: "EntityKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide the stable code used to identify this entity. It must be unique in the model. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity",
            referencingParams: [
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a61-7245-ae04-b3fdcfba0efb",
      groupKey: "model",
      actionKey: "entity_update_name",
      title: "Update entity name",
      description: "Updates the name of an entity.",
      parameters: [
        {
          name: "entityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description: "Entity to update.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this entity is located.",
          order: 10,
        },
        {
          name: "value",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this entity.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "entity",
            referencingParams: [
              {
                name: "entityRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a61-79f3-ae05-f16c070d6bd7",
      groupKey: "model",
      actionKey: "history_version_changes",
      title: "Version changes",
      description:
        "Lists the changes included in a version. When no version is provided, lists the changes since the last released version.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model whose changes you want to inspect.",
          order: 10,
        },
        {
          name: "version",
          type: "ModelVersion",
          jsonType: "string",
          optional: true,
          title: "Version",
          description:
            "Version whose changes you want to list. If not specified, the result shows the changes since the last released version. Use semantic-version format.",
          order: 20,
        },
      ],
      securityRule: "model_read",
      semantics: {
        intent: "read",
        subjects: [],
        returns: [
          "model",
          "tag",
          "entity",
          "entity_attribute",
          "relationship",
          "relationship_attribute",
        ],
      },
    },
    {
      id: "019da648-2a62-7178-be09-b151518c5087",
      groupKey: "model",
      actionKey: "history_versions",
      title: "Versions",
      description: "Lists the released versions of a model.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model whose released versions you want to list.",
          order: 10,
        },
      ],
      securityRule: "model_read",
      semantics: {
        intent: "read",
        subjects: [],
        returns: [
          "model",
          "tag",
          "entity",
          "entity_attribute",
          "relationship",
          "relationship_attribute",
        ],
      },
    },
    {
      id: "019da648-2a62-77a5-be0a-d881de227396",
      groupKey: "model",
      actionKey: "import",
      title: "Import model",
      description: "Imports a model from various locations.",
      parameters: [
        {
          name: "from",
          type: "String",
          jsonType: "string",
          optional: false,
          title: "Source to import from",
          description:
            "Source to import from.\n\n- Use an URL `https://...` to import from a remote location.\n- Use `datasource:<datasource_name>` to import from a database. Available datasources are listed in configuration tools.",
          order: 10,
        },
        {
          name: "modelKey",
          type: "ModelKey",
          jsonType: "string",
          optional: true,
          title: "Model key after import",
          description:
            "Stable business code of the model once imported. If not specified, the key is generated automatically.",
          order: 30,
        },
        {
          name: "modelName",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "Model name after import",
          description:
            "Name of the model once imported. If not specified, the name is generated automatically.",
          order: 20,
        },
      ],
      securityRule: "model_import",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a64-725a-9f7e-caf67991781d",
      groupKey: "model",
      actionKey: "inspect_models_json",
      title: "Inspect models (JSON)",
      description:
        "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model.",
      parameters: [],
      securityRule: "model_read",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a64-754b-9f7f-f6cf6bacd8f0",
      groupKey: "model",
      actionKey: "maintenance_rebuild_caches",
      title: "Maintenance rebuild caches",
      description:
        "\n            Rebuilds model application caches from stored events.\n            \n            Use this only as an exceptional maintenance action when data appears out of date.\n            If you need to run it, we recommend contacting us on the project GitHub because it\n            usually means you identified a bug.\n        ",
      parameters: [],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a64-7747-9f80-8344e9cd1da2",
      groupKey: "model",
      actionKey: "model_add_tag",
      title: "Add tag to model",
      description: "Adds a tag to a model.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to update.",
          order: 10,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to add to this model.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a64-7f6c-9f81-d1d25c43de01",
      groupKey: "model",
      actionKey: "model_copy",
      title: "Copy model",
      description:
        "Creates a copy of a model with a new key. The copied model keeps the same name and has its own lifecycle.",
      parameters: [
        {
          name: "modelNewKey",
          type: "ModelKey",
          jsonType: "string",
          optional: false,
          title: "New model key",
          description:
            "Provide a stable code for the copied model. This code is used to identify it uniquely across all models. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 20,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to copy.",
          order: 10,
        },
      ],
      securityRule: "model_copy",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelNewKey",
                kind: "key",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a65-75f7-9dc8-085947c19da7",
      groupKey: "model",
      actionKey: "model_create",
      title: "Create model",
      description:
        "Creates a new model with a key, a name, an optional description, and an optional version.",
      parameters: [
        {
          name: "description",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of this model, including what it represents, its business meaning, its role in the company or in the application, its context, its rules, its usage, and any other useful information for someone discovering it.",
          order: 40,
        },
        {
          name: "key",
          type: "ModelKey",
          jsonType: "string",
          optional: false,
          title: "Model key",
          description:
            "Provide a stable code for this model. This code is used to identify it uniquely across all models. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 30,
        },
        {
          name: "name",
          type: "LocalizedText",
          jsonType: "string",
          optional: false,
          title: "Name",
          description: "Name of this model.",
          order: 20,
        },
        {
          name: "version",
          type: "ModelVersion",
          jsonType: "string",
          optional: true,
          title: "Version",
          description:
            "Initial version of this model, using semantic-version format. If not specified, the version will be `0.0.1`.",
          order: 50,
        },
      ],
      securityRule: "model_create_manual",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "key",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a66-71ef-9927-ab4ad25cbdb7",
      groupKey: "model",
      actionKey: "model_delete",
      title: "Delete model",
      description: "Removes a model and all of its entities from the runtime.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to delete.",
          order: 10,
        },
      ],
      securityRule: "model_delete",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a66-7a41-9928-35454e41a5bf",
      groupKey: "model",
      actionKey: "model_delete_tag",
      title: "Delete tag from model",
      description: "Removes a tag from a model.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to update.",
          order: 10,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to remove from this model.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a67-707a-ad0d-fe528f4a9a1d",
      groupKey: "model",
      actionKey: "model_export",
      title: "Export model",
      description: "Returns the exported view of a model.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to export.",
          order: 10,
        },
      ],
      securityRule: "model_read",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["model"],
      },
    },
    {
      id: "019da648-2a67-74b4-ad0e-714c8ae5ac15",
      groupKey: "model",
      actionKey: "model_export_version",
      title: "Export model at a specific version",
      description:
        "Returns the exported view of a model at a specific version.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to export.",
          order: 10,
        },
        {
          name: "version",
          type: "ModelVersion",
          jsonType: "string",
          optional: false,
          title: "Version",
          description: "Version of the model to export.",
          order: 20,
        },
      ],
      securityRule: "model_read",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["model"],
      },
    },
    {
      id: "019da648-2a68-7ab4-98c6-57d7af26e704",
      groupKey: "model",
      actionKey: "model_list",
      title: "Models list",
      description: "Returns a summary list of the models.",
      parameters: [],
      securityRule: "model_read",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["model"],
      },
    },
    {
      id: "019da648-2a68-7db6-98c7-80e07a6a4a26",
      groupKey: "model",
      actionKey: "model_release",
      title: "Release version",
      description:
        "Releases a new version of a model. The new version must be greater than the previous one.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to release.",
          order: 10,
        },
        {
          name: "value",
          type: "ModelVersion",
          jsonType: "string",
          optional: false,
          title: "Version",
          description: "New version of this model.",
          order: 20,
        },
      ],
      securityRule: "model_release",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a69-743d-8c98-5061a1616792",
      groupKey: "model",
      actionKey: "model_update_authority",
      title: "Update model authority",
      description:
        "Updates whether this model serves as a canonical business reference or describes an existing system.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to update.",
          order: 10,
        },
        {
          name: "value",
          type: "ModelAuthority",
          jsonType: "string",
          optional: false,
          title: "Authority",
          description:
            "Choose whether this model serves as a canonical business reference or describes an existing system.",
          order: 20,
        },
      ],
      securityRule: "model_update_authority",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6a-7b9d-8f5f-e7e5e35dbf27",
      groupKey: "model",
      actionKey: "model_update_description",
      title: "Update model description",
      description: "Updates the description of a model.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to update.",
          order: 10,
        },
        {
          name: "value",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of this model, including what it represents, its business meaning, its role in the company or in the application, its context, its rules, its usage, and any other useful information for someone discovering it.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6b-7358-8dc7-cbc924de0812",
      groupKey: "model",
      actionKey: "model_update_documentation_link",
      title: "Update model external documentation",
      description: "Updates the external documentation link of a model.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to update.",
          order: 10,
        },
        {
          name: "value",
          type: "String",
          jsonType: "string",
          optional: true,
          title: "URL",
          description: "Link to the external documentation of this model.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6b-799d-8dc8-dbf617c91bce",
      groupKey: "model",
      actionKey: "model_update_key",
      title: "Update model key",
      description: "Updates the key of a model.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to update.",
          order: 10,
        },
        {
          name: "value",
          type: "ModelKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide the stable code used to identify this model. It must be unique across all models. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6c-7087-bd3c-afbf8efb8ff3",
      groupKey: "model",
      actionKey: "model_update_name",
      title: "Update model name",
      description: "Updates the name of a model.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model to update.",
          order: 10,
        },
        {
          name: "value",
          type: "LocalizedText",
          jsonType: "string",
          optional: false,
          title: "Name",
          description: "Name of this model.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "model",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6c-75c2-bd3d-b2dc9da98d53",
      groupKey: "model",
      actionKey: "relationship_attribute_add_tag",
      title: "Add tag to relationship attribute",
      description: "Adds a tag to a relationship attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "RelationshipAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this attribute is located.",
          order: 20,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to add to this attribute.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6c-7e8b-bd3e-5170628d4b8c",
      groupKey: "model",
      actionKey: "relationship_attribute_create",
      title: "Create relationship attribute",
      description: "Creates an attribute on a relationship.",
      parameters: [
        {
          name: "attributeKey",
          type: "AttributeKey",
          jsonType: "string",
          optional: false,
          title: "Attribute key",
          description:
            "Provide a stable code for this attribute. This code is used to identify it uniquely in the relationship. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 40,
        },
        {
          name: "description",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of what this attribute represents, what information it contains, which values are expected, the main rules that apply to it, and any useful examples or notes.",
          order: 70,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "name",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this attribute.",
          order: 30,
        },
        {
          name: "optional",
          type: "Boolean",
          jsonType: "boolean",
          optional: false,
          title: "Optional",
          description:
            "Choose whether this attribute is required for all occurrences of the relationship, or optional for some of them.",
          order: 60,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this attribute will be created.",
          order: 20,
        },
        {
          name: "type",
          type: "TypeRef",
          jsonType: "string",
          optional: false,
          title: "Data type",
          description:
            "Choose the data type of the information carried by this attribute.",
          order: 50,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "relationship_attribute",
            referencingParams: [
              {
                name: "attributeKey",
                kind: "key",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6d-7a39-a5f7-2676f34e7cee",
      groupKey: "model",
      actionKey: "relationship_attribute_delete",
      title: "Delete relationship attribute",
      description: "Deletes an attribute from a relationship.",
      parameters: [
        {
          name: "attributeRef",
          type: "RelationshipAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to delete.",
          order: 30,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this attribute is located.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "relationship_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6e-74fd-91a7-cff52bbe956a",
      groupKey: "model",
      actionKey: "relationship_attribute_delete_tag",
      title: "Delete tag from relationship attribute",
      description: "Removes a tag from a relationship attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "RelationshipAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this attribute is located.",
          order: 20,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to remove from this attribute.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6e-7dca-91a8-37f440cf323c",
      groupKey: "model",
      actionKey: "relationship_attribute_update_description",
      title: "Update relationship attribute description",
      description: "Updates the description of a relationship attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "RelationshipAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this attribute is located.",
          order: 20,
        },
        {
          name: "value",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of what this attribute represents, what information it contains, which values are expected, the main rules that apply to it, and any useful examples or notes.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6f-7618-bb14-b1858598860f",
      groupKey: "model",
      actionKey: "relationship_attribute_update_key",
      title: "Update relationship attribute key",
      description: "Updates the key of a relationship attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "RelationshipAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this attribute is located.",
          order: 20,
        },
        {
          name: "value",
          type: "AttributeKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide the stable code used to identify this attribute. It must be unique in the relationship. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a6f-7f26-bb15-83c0a9331486",
      groupKey: "model",
      actionKey: "relationship_attribute_update_name",
      title: "Update relationship attribute name",
      description: "Updates the name of a relationship attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "RelationshipAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this attribute is located.",
          order: 20,
        },
        {
          name: "value",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this attribute.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a70-76e5-bebe-b48e8edfb8e0",
      groupKey: "model",
      actionKey: "relationship_attribute_update_optional",
      title: "Update relationship attribute optionality",
      description: "Updates whether a relationship attribute is optional.",
      parameters: [
        {
          name: "attributeRef",
          type: "RelationshipAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this attribute is located.",
          order: 20,
        },
        {
          name: "value",
          type: "Boolean",
          jsonType: "boolean",
          optional: false,
          title: "Optional",
          description:
            "Choose whether this attribute is required for all occurrences of the relationship, or optional for some of them.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a70-7ea3-bebf-96c79a3c43aa",
      groupKey: "model",
      actionKey: "relationship_attribute_update_type",
      title: "Update relationship attribute type",
      description: "Updates the data type of a relationship attribute.",
      parameters: [
        {
          name: "attributeRef",
          type: "RelationshipAttributeRef",
          jsonType: "string",
          optional: false,
          title: "Attribute",
          description: "Attribute to update.",
          order: 30,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this attribute is located.",
          order: 20,
        },
        {
          name: "value",
          type: "TypeRef",
          jsonType: "string",
          optional: false,
          title: "Data type",
          description:
            "Choose the data type of the information carried by this attribute.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship_attribute",
            referencingParams: [
              {
                name: "attributeRef",
                kind: "ref",
              },
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a71-76b8-be1c-c4da84f55daa",
      groupKey: "model",
      actionKey: "relationship_role_create",
      title: "Create relationship role",
      description: "Creates a role in a relationship.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this role will be created.",
          order: 20,
        },
        {
          name: "roleCardinality",
          type: "RelationshipCardinality",
          jsonType: "string",
          optional: false,
          title: "Cardinality",
          description:
            "Choose how many occurrences of this entity may participate through this role in one occurrence of the relationship.",
          order: 60,
        },
        {
          name: "roleEntityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description:
            "Entity that participates in the relationship through this role.",
          order: 50,
        },
        {
          name: "roleKey",
          type: "RelationshipRoleKey",
          jsonType: "string",
          optional: false,
          title: "Role key",
          description:
            "Provide a stable code for this role. This code is used to identify the role within the relationship.",
          order: 40,
        },
        {
          name: "roleName",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Role name",
          description:
            "Name of this role. Use it to express how the entity participates in the relationship.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
              {
                name: "roleEntityRef",
                kind: "ref",
              },
              {
                name: "roleKey",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a75-79a1-86d4-99c8c4ec7f1a",
      groupKey: "model",
      actionKey: "relationship_role_delete",
      title: "Delete relationship role",
      description:
        "Deletes a role from a relationship. This action fails if fewer than two roles would remain.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this role is located.",
          order: 20,
        },
        {
          name: "relationshipRoleRef",
          type: "RelationshipRoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role to delete.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
              {
                name: "relationshipRoleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a76-7441-9ea6-a69c29c15d12",
      groupKey: "model",
      actionKey: "relationship_role_update_cardinality",
      title: "Update relationship role cardinality",
      description: "Updates the cardinality of a relationship role.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this role is located.",
          order: 20,
        },
        {
          name: "relationshipRoleRef",
          type: "RelationshipRoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role to update.",
          order: 30,
        },
        {
          name: "value",
          type: "RelationshipCardinality",
          jsonType: "string",
          optional: false,
          title: "Cardinality",
          description:
            "Choose how many occurrences of this entity may participate through this role in one occurrence of the relationship.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
              {
                name: "relationshipRoleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a76-7ec8-9ea7-5911fed8ac1e",
      groupKey: "model",
      actionKey: "relationship_role_update_entity",
      title: "Update relationship role entity",
      description:
        "Updates which entity participates through a relationship role.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this role is located.",
          order: 20,
        },
        {
          name: "relationshipRoleRef",
          type: "RelationshipRoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role to update.",
          order: 30,
        },
        {
          name: "value",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Entity",
          description:
            "Entity that participates in the relationship through this role.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
              {
                name: "relationshipRoleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a78-7b26-8ff1-4d17fe48f960",
      groupKey: "model",
      actionKey: "relationship_role_update_key",
      title: "Update relationship role key",
      description: "Updates the key of a relationship role.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this role is located.",
          order: 20,
        },
        {
          name: "relationshipRoleRef",
          type: "RelationshipRoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role to update.",
          order: 30,
        },
        {
          name: "value",
          type: "RelationshipRoleKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide the stable code used to identify this role within the relationship.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
              {
                name: "relationshipRoleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a79-76b8-9b8a-a780eb23d20b",
      groupKey: "model",
      actionKey: "relationship_role_update_name",
      title: "Update relationship role name",
      description: "Updates the name of a relationship role.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship where this role is located.",
          order: 20,
        },
        {
          name: "relationshipRoleRef",
          type: "RelationshipRoleRef",
          jsonType: "string",
          optional: false,
          title: "Role",
          description: "Role to update.",
          order: 30,
        },
        {
          name: "value",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description:
            "Name of this role. Use it to express how the entity participates in the relationship.",
          order: 40,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
              {
                name: "relationshipRoleRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a7a-7378-ac47-dcc60423d51e",
      groupKey: "model",
      actionKey: "relationship_add_tag",
      title: "Add tag to relationship",
      description: "Adds a tag to a relationship.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship to update.",
          order: 20,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to add to this relationship.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a7b-79a1-86d1-38e4c67b6b1b",
      groupKey: "model",
      actionKey: "relationship_create",
      title: "Create relationship",
      description: "Creates a relationship between entities in a model.",
      parameters: [
        {
          name: "description",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of what this relationship represents in the domain. Explain what link or fact it expresses between entities, the main rules that apply to it, how to read it, and any useful examples or notes.",
          order: 40,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship will be created.",
          order: 10,
        },
        {
          name: "name",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this relationship.",
          order: 20,
        },
        {
          name: "relationshipKey",
          type: "RelationshipKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide a stable code for this relationship. This code is used to identify it uniquely in the model. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 30,
        },
        {
          name: "roleACardinality",
          type: "RelationshipCardinality",
          jsonType: "string",
          optional: false,
          title: "Role A cardinality",
          description:
            "Choose how many occurrences of this entity may participate through the first role in one occurrence of the relationship.",
          order: 80,
        },
        {
          name: "roleAEntityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Role A entity",
          description:
            "Entity that participates in this relationship through the first role.",
          order: 70,
        },
        {
          name: "roleAKey",
          type: "RelationshipRoleKey",
          jsonType: "string",
          optional: false,
          title: "Role A key",
          description:
            "Provide a stable code for the first role in this relationship. This code is used to identify the role within the relationship.",
          order: 60,
        },
        {
          name: "roleAName",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Role A name",
          description:
            "Name of the first role. Use it to express how this entity participates in the relationship.",
          order: 50,
        },
        {
          name: "roleBCardinality",
          type: "RelationshipCardinality",
          jsonType: "string",
          optional: false,
          title: "Role B cardinality",
          description:
            "Choose how many occurrences of this entity may participate through the second role in one occurrence of the relationship.",
          order: 120,
        },
        {
          name: "roleBEntityRef",
          type: "EntityRef",
          jsonType: "string",
          optional: false,
          title: "Role B entity",
          description:
            "Entity that participates in this relationship through the second role.",
          order: 110,
        },
        {
          name: "roleBKey",
          type: "RelationshipRoleKey",
          jsonType: "string",
          optional: false,
          title: "Role B key",
          description:
            "Provide a stable code for the second role in this relationship. This code is used to identify the role within the relationship.",
          order: 100,
        },
        {
          name: "roleBName",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Role B name",
          description:
            "Name of the second role. Use it to express how this entity participates in the relationship.",
          order: 90,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipKey",
                kind: "key",
              },
              {
                name: "roleAEntityRef",
                kind: "ref",
              },
              {
                name: "roleAKey",
                kind: "key",
              },
              {
                name: "roleBEntityRef",
                kind: "ref",
              },
              {
                name: "roleBKey",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a7c-7662-8303-e0cee2b02657",
      groupKey: "model",
      actionKey: "relationship_delete",
      title: "Delete relationship",
      description: "Deletes a relationship.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship to delete.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a7c-7c5a-8304-041cfdea9872",
      groupKey: "model",
      actionKey: "relationship_delete_tag",
      title: "Delete relationship tag",
      description: "Removes a tag from a relationship.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship to update.",
          order: 20,
        },
        {
          name: "tag",
          type: "TagRef",
          jsonType: "string",
          optional: false,
          title: "Tag",
          description: "Tag to remove from this relationship.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a7d-7191-83e7-436c5e36d96d",
      groupKey: "model",
      actionKey: "relationship_update_description",
      title: "Update relationship description",
      description: "Updates the description of a relationship.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship to update.",
          order: 20,
        },
        {
          name: "value",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of what this relationship represents in the domain. Explain what link or fact it expresses between entities, the main rules that apply to it, how to read it, and any useful examples or notes.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a7d-773b-83e8-bdbfe8d5482a",
      groupKey: "model",
      actionKey: "relationship_update_key",
      title: "Update relationship key",
      description: "Updates the key of a relationship.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship to update.",
          order: 20,
        },
        {
          name: "value",
          type: "RelationshipKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide the stable code used to identify this relationship. It must be unique in the model. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a7e-71ae-b992-3e1e31e41de5",
      groupKey: "model",
      actionKey: "relationship_update_name",
      title: "Update relationship name",
      description: "Updates the name of a relationship.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this relationship is located.",
          order: 10,
        },
        {
          name: "relationshipRef",
          type: "RelationshipRef",
          jsonType: "string",
          optional: false,
          title: "Relationship",
          description: "Relationship to update.",
          order: 20,
        },
        {
          name: "value",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this relationship.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "relationship",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "relationshipRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a7e-7849-b993-d8ec836a871b",
      groupKey: "model",
      actionKey: "search",
      title: "Search",
      description: "Searches models and model objects.",
      parameters: [
        {
          name: "fields",
          type: "SearchFields",
          jsonType: "array",
          optional: false,
          title: "Fields",
          description:
            "Fields returned for each search result. Use this to choose which information is included in the result, for example the location.",
          order: 20,
        },
        {
          name: "filters",
          type: "SearchFilters",
          jsonType: "object",
          optional: false,
          title: "Filters",
          description:
            "Filters used to narrow the search result. You can combine text filters and tag filters with AND or OR.",
          order: 10,
        },
      ],
      securityRule: "model_read",
      semantics: {
        intent: "read",
        subjects: [],
        returns: [
          "model",
          "tag",
          "entity",
          "entity_attribute",
          "relationship",
          "relationship_attribute",
        ],
      },
    },
    {
      id: "019da648-2a7f-7164-a7c4-deb74a88d15c",
      groupKey: "model",
      actionKey: "type_create",
      title: "Create type",
      description: "Creates a data type in an existing model.",
      parameters: [
        {
          name: "description",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of this data type, including how and where to use it, business rules, constraints or possible values.",
          order: 40,
        },
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this data type will be created.",
          order: 10,
        },
        {
          name: "name",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this data type.",
          order: 20,
        },
        {
          name: "typeKey",
          type: "TypeKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide a stable code for this data type. This code is used to identify it uniquely in the model. Use a short value, keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "create",
        subjects: [
          {
            type: "type",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "typeKey",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a7f-7f99-a7c5-d0ff2587887c",
      groupKey: "model",
      actionKey: "type_delete",
      title: "Delete type",
      description:
        "Deletes a data type from a model. This action fails if the data type is still used by entity attributes.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this data type is located.",
          order: 10,
        },
        {
          name: "typeRef",
          type: "TypeRef",
          jsonType: "string",
          optional: false,
          title: "Type",
          description: "Data type to delete.",
          order: 20,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "delete",
        subjects: [
          {
            type: "type",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "typeRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a80-75b2-8d66-57a029866538",
      groupKey: "model",
      actionKey: "type_update_description",
      title: "Update type description",
      description: "Updates the description of a data type.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this data type is located.",
          order: 10,
        },
        {
          name: "typeRef",
          type: "TypeRef",
          jsonType: "string",
          optional: false,
          title: "Type",
          description: "Data type to update.",
          order: 20,
        },
        {
          name: "value",
          type: "LocalizedMarkdown",
          jsonType: "string",
          optional: true,
          title: "Description",
          description:
            "Provide a comprehensive description of this data type, including how and where to use it, business rules, constraints or possible values.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "type",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "typeRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a80-7dba-8d67-9af540bdf623",
      groupKey: "model",
      actionKey: "type_update_key",
      title: "Update type key",
      description: "Updates the key of a data type.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this data type is located.",
          order: 10,
        },
        {
          name: "typeRef",
          type: "TypeRef",
          jsonType: "string",
          optional: false,
          title: "Type",
          description: "Data type to update.",
          order: 20,
        },
        {
          name: "value",
          type: "TypeKey",
          jsonType: "string",
          optional: false,
          title: "Key",
          description:
            "Provide the stable code used to identify this data type. It must be unique in the model. Keep it stable over time, and avoid quotes, backslashes, and unusual special characters.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "type",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "typeRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a81-7672-b227-144d4eeef1c1",
      groupKey: "model",
      actionKey: "type_update_name",
      title: "Update type name",
      description: "Updates the name of a data type.",
      parameters: [
        {
          name: "modelRef",
          type: "ModelRef",
          jsonType: "string",
          optional: false,
          title: "Model",
          description: "Model where this data type is located.",
          order: 10,
        },
        {
          name: "typeRef",
          type: "TypeRef",
          jsonType: "string",
          optional: false,
          title: "Type",
          description: "Data type to update.",
          order: 20,
        },
        {
          name: "value",
          type: "LocalizedText",
          jsonType: "string",
          optional: true,
          title: "Name",
          description: "Name of this data type.",
          order: 30,
        },
      ],
      securityRule: "model_write",
      semantics: {
        intent: "update",
        subjects: [
          {
            type: "type",
            referencingParams: [
              {
                name: "modelRef",
                kind: "ref",
              },
              {
                name: "typeRef",
                kind: "ref",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019da648-2a82-761c-b996-8b666d1f0601",
      groupKey: "config",
      actionKey: "ai_agents_instructions",
      title: "AI Agents Instructions",
      description:
        "Each AI Agent should read that first. Returns a usage guide for AI Agents. Use it for your AGENTS.md files if your agent doesn't support instructions in MCP.",
      parameters: [],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a82-7c62-b997-ea82313fae50",
      groupKey: "config",
      actionKey: "inspect_config_text",
      title: "Inspect config as text file",
      description:
        "Returns a human-readable list of the configuration, including extension contributions and contribution points, what provides what to whom.",
      parameters: [],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a83-7102-8a99-37576a7a4fd3",
      groupKey: "config",
      actionKey: "inspect_actions",
      title: "Inspect actions",
      description:
        "Returns all known actions with their parameter descriptions.",
      parameters: [],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a83-739d-8a9a-d7009db7cd68",
      groupKey: "config",
      actionKey: "inspect_actions_all",
      title: "Inspect all actions",
      description: "Returns all known from the system.",
      parameters: [],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a83-75df-8a9b-73fc59758cdc",
      groupKey: "config",
      actionKey: "inspect_config",
      title: "Inspect config",
      description:
        "Returns a Json representation of the configuration, including extension contributions and contribution points, what provides what to whom.",
      parameters: [],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a83-78e9-8a9c-3bfcba76ee00",
      groupKey: "config",
      actionKey: "inspect_permissions",
      title: "Inspect permissions",
      description:
        "Returns all known permissions registered in application with their descriptions.",
      parameters: [],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a83-7c04-8a9d-ce485e64592c",
      groupKey: "config",
      actionKey: "inspect_security_rules",
      title: "Inspect security rules",
      description:
        "Returns all known security rules registered in application with their descriptions.",
      parameters: [],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a83-7edd-8a9e-b4f86730ce90",
      groupKey: "config",
      actionKey: "inspect_type_system",
      title: "Inspect type system",
      description:
        "Returns all known types declared in application with their description.",
      parameters: [],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a84-7628-84ed-43841ab61ced",
      groupKey: "databases",
      actionKey: "driver_list",
      title: "Database drivers",
      description: "Lists available database drivers",
      parameters: [],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019da648-2a84-7f85-84ee-b108fbb3fdc9",
      groupKey: "databases",
      actionKey: "datasource_list",
      title: "Database sources",
      description: "Lists available datasources",
      parameters: [],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
  ],
} as const satisfies ActionRegistryDto;
