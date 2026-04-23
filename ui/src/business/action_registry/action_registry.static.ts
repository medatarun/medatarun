import type { ActionRegistryDto } from "@/business/action_registry/action_registry.dto.ts";

export const actionRegistryStatic = {
  items: [
    {
      id: "019db951-1e3b-731e-b205-4b0e16217ad0",
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
      id: "019db951-1e45-731a-a382-5c2358b74ab8",
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
      id: "019db951-1e47-7087-b024-37f88467d2b8",
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
      id: "019db951-1e47-7a9b-b025-e200af9aa755",
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
      id: "019db951-1e4a-7791-aede-60207de641f9",
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
      id: "019db951-1e4b-7537-953c-f402f7c9a691",
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
      id: "019db951-1e4c-7287-8da6-cceb35554028",
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
      id: "019db951-1e4c-7c0c-8da7-4370f54e1dac",
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
      id: "019db951-1e4e-7f85-ae80-aa55a146d79c",
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
      id: "019db951-1e4f-793b-a314-d1be90b748dc",
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
      id: "019db951-1e50-7414-aab3-0511a40f7958",
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
      id: "019db951-1e51-7ae9-afd9-a5070d877046",
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
      id: "019db951-1e52-7c8b-979b-b2fc05ace9dc",
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
      id: "019db951-1e53-7b70-a5c3-fb28ad5a0264",
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
      id: "019db951-1e54-7abc-b7f4-60656757b50a",
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
      id: "019db951-1e55-78c4-84f7-53324f67d841",
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
      id: "019db951-1e56-7266-a29d-c55b6a29190e",
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
      id: "019db951-1e57-73a1-b3a7-e5401d813ffc",
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
      id: "019db951-1e58-707a-9af4-94681c8e0624",
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
      id: "019db951-1e59-73be-9023-6d49ee605103",
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
        intent: "update",
        subjects: [
          {
            type: "user",
            referencingParams: [
              {
                name: "username",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019db951-1e59-7dba-9024-3bf26078580a",
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
      id: "019db951-1e5a-7c9b-abec-a5a889030c7e",
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
      id: "019db951-1e5b-7933-a81f-ba79fe54c2c6",
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
        intent: "update",
        subjects: [
          {
            type: "user",
            referencingParams: [
              {
                name: "username",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019db951-1e5c-749f-804e-1d653c231a18",
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
        intent: "update",
        subjects: [
          {
            type: "user",
            referencingParams: [
              {
                name: "username",
                kind: "key",
              },
            ],
          },
        ],
        returns: [],
      },
    },
    {
      id: "019db951-1e5d-72a3-b4eb-aab97922cb96",
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
      id: "019db951-1e5e-71c6-8b3f-609592c13e20",
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
      id: "019db951-1e5f-7d0e-8a8b-65a44d7dca7a",
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
      id: "019db951-1e60-7262-a78c-a8fed7dca340",
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
      id: "019db951-1e61-77ba-8e9c-8aa7a8827353",
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
      id: "019db951-1e61-7f1a-8e9d-65fc5570124c",
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
      id: "019db951-1e62-7610-8f15-77644ef52f4f",
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
      id: "019db951-1e63-718d-81cf-6778aad9a9ae",
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
      id: "019db951-1e63-7e35-81d0-c91cbe70d583",
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
      id: "019db951-1e64-793f-8fe3-29a9f8095bd4",
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
      id: "019db951-1e64-7e87-8fe4-0b6d3217bf8c",
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
      id: "019db951-1e65-724d-b2e4-3543cb732aa3",
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
      id: "019db951-1e65-77df-b2e5-d13060cce8a1",
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
      id: "019db951-1e65-7e08-b2e6-2a2d79c285bb",
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
      id: "019db951-1e66-76c4-8329-e49eda24ba5f",
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
      id: "019db951-1e68-7b3b-aa77-a2b4029c2160",
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
      id: "019db951-1e69-7326-be9e-a5a4514474a4",
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
      id: "019db951-1e69-7bc2-be9f-70ec5be62dbf",
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
      id: "019db951-1e6a-725a-83f2-8cba5fbe9976",
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
      id: "019db951-1e6a-7872-83f3-fe45c6737c3e",
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
      id: "019db951-1e70-7f12-9123-0bd2e85e9493",
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
      id: "019db951-1e73-7dd7-bf7f-dc807a19fb79",
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
      id: "019db951-1e74-7c87-85d9-1b64c340e16a",
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
      id: "019db951-1e76-7d99-8734-089d0b06a151",
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
      id: "019db951-1e78-704d-bc0a-229a20ae1519",
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
      id: "019db951-1e79-7435-b414-d3fe597b2df1",
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
      id: "019db951-1e7a-71fb-8e0b-cb4498753f21",
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
      id: "019db951-1e7b-7bca-afde-e6c6bf8d6354",
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
      id: "019db951-1e7c-7789-a8a4-6212ce35f324",
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
      id: "019db951-1e7e-7418-8380-36dbe62a9fd6",
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
      id: "019db951-1e7e-7f33-8381-548046a64b05",
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
      id: "019db951-1e7f-7c49-b9d7-bb4bf42f04f1",
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
      id: "019db951-1e82-7506-94c8-b5c1d065122e",
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
      id: "019db951-1e83-74c8-872f-f7bfe1183749",
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
      id: "019db951-1e85-702d-8165-0f42df7a3c56",
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
      id: "019db951-1e85-7a93-8166-4e45c9dda271",
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
      id: "019db951-1e86-7795-a5d0-3e0c89df1aec",
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
      id: "019db951-1e87-721c-9642-dbe3b73cfc80",
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
      id: "019db951-1e87-7a83-9643-6ec1597bda0d",
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
      id: "019db951-1e88-7e93-8924-2a52b2788464",
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
      id: "019db951-1e89-75db-b1bd-393bf4da87ac",
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
      id: "019db951-1e89-7cf5-b1be-7f1b1ee24a62",
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
      id: "019db951-1e8a-76b0-9e41-b561e7817ce3",
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
      id: "019db951-1e8a-7e5e-9e42-540d29eb7e2e",
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
      id: "019db951-1e8b-7649-8f6b-fb879aac6eef",
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
      id: "019db951-1e8b-7e97-8f6c-e6950b8ebdbb",
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
      id: "019db951-1e8c-77be-ab30-3179cf89dcf1",
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
      id: "019db951-1e8c-7ee5-ab31-ccb63875ad87",
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
      id: "019db951-1e8e-7afd-96f2-6f34fe0bc2b5",
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
      id: "019db951-1e8e-7e2d-96f3-72c17be2048e",
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
      id: "019db951-1e8e-7ff7-96f4-1e61f2d69fda",
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
      id: "019db951-1e8f-785e-8bf6-fa50a4d237b5",
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
      id: "019db951-1e8f-7ed0-8bf7-0553ff694856",
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
      id: "019db951-1e90-77df-928d-3554eedeaa11",
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
      id: "019db951-1e90-7e28-928e-725a18064774",
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
      id: "019db951-1e91-77d2-aabf-7a34aa543c3b",
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
      id: "019db951-1e91-7cb8-aac0-fae43b1b2170",
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
      id: "019db951-1e93-7143-93b9-590220f65316",
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
      id: "019db951-1e93-73d2-93ba-db27befd49fc",
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
      id: "019db951-1e93-79fb-93bb-421c3e2313ba",
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
      id: "019db951-1e95-7245-a980-098bd2a01ca1",
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
      id: "019db951-1e95-7bd2-a981-cfe506443baa",
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
      id: "019db951-1e96-755c-9329-4d5951b03168",
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
      id: "019db951-1e96-7d47-932a-b25fb3539d8b",
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
      id: "019db951-1e97-7424-9b76-7fb03d447241",
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
      id: "019db951-1e97-7ed0-9b77-eb1787933008",
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
      id: "019db951-1e98-7a39-b5f4-6003925cbc36",
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
      id: "019db951-1e99-7160-bf17-4a8b02e10024",
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
      id: "019db951-1e99-7c4d-bf18-638b3c2d9f9c",
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
      id: "019db951-1e9a-749f-8d58-ac9015f93223",
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
      id: "019db951-1e9a-7cb0-8d59-055b461582c5",
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
      id: "019db951-1e9b-76f9-bdb1-9e5c6b15d52f",
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
      id: "019db951-1e9c-70c4-af3a-5d5ccb722e41",
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
      id: "019db951-1e9c-78f9-af3b-fd45b26ad713",
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
      id: "019db951-1e9d-7558-9562-c190875d344c",
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
      id: "019db951-1e9d-7c5e-9563-9b429b9e63b2",
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
      id: "019db951-1ea2-715c-bdb6-5ebf9642b405",
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
      id: "019db951-1ea2-7e97-bdb7-3531670675b4",
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
      id: "019db951-1ea4-71f7-8737-617d40bf239e",
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
      id: "019db951-1ea4-7df3-8738-008dc9ceab87",
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
      id: "019db951-1ea5-7676-9a43-a0ad34b2ade4",
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
      id: "019db951-1ea6-76d9-ba5f-f237bda783aa",
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
      id: "019db951-1ea6-7e28-ba60-4da47aba78c7",
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
      id: "019db951-1ea7-77eb-8948-195af4eacbe9",
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
      id: "019db951-1ea8-727a-a6ab-c7a530a5cfab",
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
      id: "019db951-1ea8-7f26-a6ac-6c8586fc7bb3",
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
      id: "019db951-1ea9-79ce-9749-fc43f0c561c2",
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
      id: "019db951-1eaa-7f99-b970-8c2623578baa",
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
      id: "019db951-1eab-79df-abfe-3deb7634c654",
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
      id: "019db951-1eac-73c2-957d-1c39461e1e81",
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
      id: "019db951-1ead-72b4-b97e-d034ff11b7e1",
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
      id: "019db951-1ead-7da1-b97f-08b0eaabea73",
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
      id: "019db951-1eaf-7218-b253-39c868351bcc",
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
      id: "019db951-1eaf-7743-b254-2023abeb1e16",
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
      id: "019db951-1eaf-79ae-b255-ef43ce029055",
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
      id: "019db951-1eaf-7ec8-b256-55b5593c1a9a",
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
      id: "019db951-1eb0-7249-96cf-027941e07ff2",
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
      id: "019db951-1eb0-7893-96d0-7d697f7e54fc",
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
      id: "019db951-1eb0-7c18-96d1-cc32f40628b2",
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
      id: "019db951-1eb0-7e56-96d2-20e39f0128c6",
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
      id: "019db951-1eb1-7e6a-8cdb-20f5a8aa69b1",
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
      id: "019db951-1eb2-71df-aa57-871fcb427fb8",
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
