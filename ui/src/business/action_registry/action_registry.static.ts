import type { ActionRegistryDto } from "@/business/action_registry/action_registry.dto.ts";

export const actionRegistryStatic = {
  items: [
    {
      id: "019d9b05-2495-717c-8697-51c3c71c9ce6",
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
      uiLocations: ["global"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-249f-7c87-8125-aafc02670142",
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
      uiLocations: ["auth_actor"],
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
      id: "019d9b05-24a1-7fc2-aa9b-4aa3eba0a693",
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
      uiLocations: ["auth_actor"],
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
      id: "019d9b05-24a3-774b-9911-990b82e13d36",
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
      uiLocations: ["hidden"],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["actor"],
      },
    },
    {
      id: "019d9b05-24a5-75e3-ab4a-b3d0af284dd1",
      groupKey: "auth",
      actionKey: "actor_list",
      title: "List actors",
      description:
        "List all known actors: all actors maintained by Medatarun and also all external actor that have connected at least once. Only available for admins.",
      parameters: [],
      uiLocations: ["hidden"],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["actor"],
      },
    },
    {
      id: "019d9b05-24a6-720c-8b28-7039f0bb653a",
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
      uiLocations: ["auth_actor_roles"],
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
      id: "019d9b05-24a7-7266-afe3-db25efe13947",
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
      uiLocations: ["auth_actor_role"],
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
      id: "019d9b05-24a7-7f06-afe4-c0fe37413314",
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
      uiLocations: ["hidden"],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24aa-752b-b0c8-ec2661829e90",
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
      uiLocations: ["hidden"],
      securityRule: "signed_in",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24aa-7f6c-b0c9-5ce317ca1b19",
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
      uiLocations: ["hidden"],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24ab-7a24-8152-79888b9c0b8a",
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
      uiLocations: ["auth_role.permissions"],
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
      id: "019d9b05-24ac-7e3d-8c71-15dc4ba3a9f1",
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
      uiLocations: ["auth_roles"],
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
      id: "019d9b05-24ae-71be-9a5e-78d12e0883ad",
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
      uiLocations: ["auth_role"],
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
      id: "019d9b05-24af-713b-aeca-c1f610d6e837",
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
      uiLocations: ["auth_role.permission"],
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
      id: "019d9b05-24b0-713f-b6a6-d3d71f2d7da4",
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
      uiLocations: ["hidden"],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["role"],
      },
    },
    {
      id: "019d9b05-24b1-75b2-b570-1e42064ca3fa",
      groupKey: "auth",
      actionKey: "role_list",
      title: "List roles",
      description: "List all roles.",
      parameters: [],
      uiLocations: ["hidden"],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["role"],
      },
    },
    {
      id: "019d9b05-24b2-7220-834a-da37d846a3d4",
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
      uiLocations: ["hidden"],
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
      id: "019d9b05-24b3-767e-a084-ce0919f5cde1",
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
      uiLocations: ["auth_role"],
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
      id: "019d9b05-24b4-7418-bdbc-5577a4a854a1",
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
      uiLocations: ["hidden"],
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
      id: "019d9b05-24b5-79f7-96e3-16dadf564848",
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
      uiLocations: ["user"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24b6-730a-a5ef-bf9ab31b3ffe",
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
      uiLocations: ["user"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24b6-7cbc-a5f0-13c3de98bbf8",
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
      uiLocations: ["users"],
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
      id: "019d9b05-24b7-7a8f-b77a-e40190f279f1",
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
      uiLocations: ["user"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24b8-7522-bc00-16c126e985e2",
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
      uiLocations: ["user"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24b9-73be-9720-229d6333f560",
      groupKey: "auth",
      actionKey: "user_list",
      title: "User list",
      description: "Lists available users. Only available for admins.",
      parameters: [],
      uiLocations: ["users"],
      securityRule: "admin",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["user"],
      },
    },
    {
      id: "019d9b05-24ba-787e-9835-0e61d65e7488",
      groupKey: "auth",
      actionKey: "whoami",
      title: "Who am i",
      description:
        "Tells who is the connected user. Allow you to know if you have the credentials you need",
      parameters: [],
      uiLocations: ["hidden"],
      securityRule: "signed_in",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24bc-760c-810b-b3d3f701fc41",
      groupKey: "tag",
      actionKey: "maintenance_rebuild_caches",
      title: "Maintenance rebuild caches",
      description:
        "\n            Rebuilds tag application caches from stored events.\n            \n            Use this only as an exceptional maintenance action when data appears out of date.\n            If you need to run it, we recommend contacting us on the project GitHub because it\n            usually means you identified a bug.\n        ",
      parameters: [],
      uiLocations: ["hidden"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24bc-7b6c-810c-29bba6ac2520",
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
      uiLocations: ["tag_global_list"],
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
      id: "019d9b05-24be-7364-83fe-5b22a824df0b",
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
      uiLocations: ["tag_global_detail"],
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
      id: "019d9b05-24be-7d68-83ff-439f23ce4a86",
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
      uiLocations: ["tag_global_detail"],
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
      id: "019d9b05-24bf-75ef-bb69-e85f2f9b8879",
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
      uiLocations: ["tag_global_detail"],
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
      id: "019d9b05-24c0-7404-96a2-388be238b401",
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
      uiLocations: ["tag_global_detail"],
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
      id: "019d9b05-24c1-70a7-9fc6-f256fd48bcdb",
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
      uiLocations: ["tag_group_list"],
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
      id: "019d9b05-24c1-7dd2-9fc7-095a1eaf8e10",
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
      uiLocations: ["tag_group_detail"],
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
      id: "019d9b05-24c2-76f1-9850-c2fa9c98556e",
      groupKey: "tag",
      actionKey: "tag_group_list",
      title: "Tag group list",
      description: "Lists all tag groups.",
      parameters: [],
      uiLocations: ["hidden"],
      securityRule: "signed_in",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["tag_group"],
      },
    },
    {
      id: "019d9b05-24c2-7aed-9851-947785353937",
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
      uiLocations: ["tag_group_detail"],
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
      id: "019d9b05-24c3-70d0-9482-0bd5cbd039ee",
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
      uiLocations: ["tag_group_detail"],
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
      id: "019d9b05-24c3-7835-9483-98585a732236",
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
      uiLocations: ["tag_group_detail"],
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
      id: "019d9b05-24c4-7287-94de-6fc6114173ad",
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
      uiLocations: ["tag_local_list"],
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
      id: "019d9b05-24c5-7d4b-8c63-43786f08559d",
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
      uiLocations: ["tag_local_detail"],
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
      id: "019d9b05-24c6-7b81-a5f8-1b73953597fd",
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
      uiLocations: ["tag_local_detail"],
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
      id: "019d9b05-24c7-744d-be1a-6669888409ad",
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
      uiLocations: ["tag_local_detail"],
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
      id: "019d9b05-24c7-7bfb-be1b-a7c213389dad",
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
      uiLocations: ["tag_local_detail"],
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
      id: "019d9b05-24c8-7256-b6aa-1b035ff2a1fa",
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
      uiLocations: ["hidden"],
      securityRule: "signed_in",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["tag"],
      },
    },
    {
      id: "019d9b05-24cd-7cd9-ab4b-2e249a8bf624",
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
      uiLocations: ["entity.business_keys"],
      securityRule: "signed_in",
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
      id: "019d9b05-24d0-7d68-8d23-60b9449cfb11",
      groupKey: "model",
      actionKey: "business_key_delete",
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
      uiLocations: ["entity.business_key"],
      securityRule: "signed_in",
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
      id: "019d9b05-24d2-73ba-a329-5de1031d116b",
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
      uiLocations: ["entity.business_key"],
      securityRule: "signed_in",
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
      id: "019d9b05-24d4-7553-8670-82ba485f849e",
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
      uiLocations: ["entity.business_key"],
      securityRule: "signed_in",
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
      id: "019d9b05-24d5-7210-89c8-3f7ab34c3e5b",
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
      uiLocations: ["entity.business_key"],
      securityRule: "signed_in",
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
      id: "019d9b05-24d6-7487-9414-b0821d25fec6",
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
      uiLocations: ["entity.business_key"],
      securityRule: "signed_in",
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
      id: "019d9b05-24d7-7585-bc6e-92049ea2bfa4",
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
      uiLocations: ["global"],
      securityRule: "signed_in",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24d9-7414-8fe3-fa9f867e12ce",
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
      uiLocations: ["entity.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24da-75be-9fb6-4e5684d74019",
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
      uiLocations: ["entity.attributes"],
      securityRule: "signed_in",
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
      id: "019d9b05-24dc-760c-8282-d6dfa9649444",
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
      uiLocations: ["entity.attribute"],
      securityRule: "signed_in",
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
      id: "019d9b05-24de-7ca3-939c-67e4f7752cc1",
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
      uiLocations: ["entity.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24df-79ce-863d-4b43fb8656f2",
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
      uiLocations: ["entity.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e0-7b4f-bf28-4a6bba2fed51",
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
      uiLocations: ["entity.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e1-7fbe-85c1-e0d3110c2458",
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
      uiLocations: ["entity.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e3-7aa3-bbc2-c52513b769bb",
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
      uiLocations: ["entity.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e4-7ad9-9bda-a78536327707",
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
      uiLocations: ["entity.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e5-781c-a206-82b2965c031c",
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
      uiLocations: ["entity"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e6-76b0-85ea-e65e8078db46",
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
      uiLocations: ["entity.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e7-70e5-ad71-d3dca8bc5f92",
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
      uiLocations: ["model.entities"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e8-7262-9c85-19016b92789f",
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
      uiLocations: ["entity"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e8-79ca-9c86-bffacab16116",
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
      uiLocations: ["entity.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e9-727a-8a17-eb7576fb960a",
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
      uiLocations: ["entity.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24e9-7c14-8a18-277a5023b375",
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
      uiLocations: ["entity.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24ea-7493-a1d9-cd7bde9f17b9",
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
      uiLocations: ["entity.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24ea-7e9b-a1da-018cf277d138",
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
      uiLocations: ["entity.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24eb-7a28-9247-4b8f0ee83770",
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
      uiLocations: ["hidden"],
      securityRule: "signed_in",
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
      id: "019d9b05-24ec-71b2-8cb0-8a1ca91c688d",
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
      uiLocations: ["hidden"],
      securityRule: "signed_in",
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
      id: "019d9b05-24ec-7c35-8cb1-a4e7ce82877f",
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
      uiLocations: ["models"],
      securityRule: "signed_in",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24ee-7c97-8f41-fe31e8f7dfc9",
      groupKey: "model",
      actionKey: "inspect_models_json",
      title: "Inspect models (JSON)",
      description:
        "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model.",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "signed_in",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24ee-7fc2-8f42-cf1b6f94b1ff",
      groupKey: "model",
      actionKey: "maintenance_rebuild_caches",
      title: "Maintenance rebuild caches",
      description:
        "\n            Rebuilds model application caches from stored events.\n            \n            Use this only as an exceptional maintenance action when data appears out of date.\n            If you need to run it, we recommend contacting us on the project GitHub because it\n            usually means you identified a bug.\n        ",
      parameters: [],
      uiLocations: ["hidden"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-24ef-7199-8f69-8e2c1df0a8d6",
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
      uiLocations: ["model.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24ef-795c-8f6a-1d6714428296",
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
      uiLocations: ["model.overview"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f0-70b4-934f-775e43c33184",
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
      uiLocations: ["models"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f0-79df-9350-5d328f2c5aa1",
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
      uiLocations: ["model.overview"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f0-7ee1-9351-0bad14ad1fbf",
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
      uiLocations: ["model.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f1-7849-b203-e0197c39cb14",
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
      uiLocations: ["hidden"],
      securityRule: "signed_in",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["model"],
      },
    },
    {
      id: "019d9b05-24f1-7c87-b204-6542c6be2cd6",
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
      uiLocations: ["hidden"],
      securityRule: "signed_in",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["model"],
      },
    },
    {
      id: "019d9b05-24f3-705a-a0b0-75c7cc747cde",
      groupKey: "model",
      actionKey: "model_list",
      title: "Models list",
      description: "Returns a summary list of the models.",
      parameters: [],
      uiLocations: ["hidden"],
      securityRule: "signed_in",
      semantics: {
        intent: "read",
        subjects: [],
        returns: ["model"],
      },
    },
    {
      id: "019d9b05-24f3-75df-a0b1-be0b38d0bcc7",
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
      uiLocations: ["model.overview"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f4-70a7-aae1-b8694b1d39de",
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
      uiLocations: ["model.overview"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f5-765e-bd2f-502f2751a596",
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
      uiLocations: ["model.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f6-7010-9636-4fc8685a089c",
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
      uiLocations: ["model.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f6-7712-9637-7653bcc3e8fb",
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
      uiLocations: ["model.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f6-7d70-9638-3f0002304729",
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
      uiLocations: ["model.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f7-7410-93ef-0d1061be1a46",
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
      uiLocations: ["relationship.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f7-7ddf-93f0-2b858dbe0d30",
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
      uiLocations: ["relationship.attributes"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f8-78b8-bb72-85e078ec5b36",
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
      uiLocations: ["relationship.attribute"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f9-7439-9ffe-0b8dc4a038bd",
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
      uiLocations: ["relationship.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24f9-7cd9-9fff-88cbf6a90dfa",
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
      uiLocations: ["relationship.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24fa-754b-9199-2daffec90884",
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
      uiLocations: ["relationship.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24fb-701c-9c95-2860e82ac758",
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
      uiLocations: ["relationship.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24fb-7926-9c96-3d405b007ea7",
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
      uiLocations: ["relationship.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24fc-729f-bd2a-88ace320232b",
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
      uiLocations: ["relationship.attribute.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-24fc-7b70-bd2b-bcb1d3a2174f",
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
      uiLocations: ["relationship.roles"],
      securityRule: "signed_in",
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
      id: "019d9b05-24fd-794b-8887-0878988a6ca9",
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
      uiLocations: ["relationship.role"],
      securityRule: "signed_in",
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
      id: "019d9b05-2501-7ab8-a1b7-7607c1a72eac",
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
      uiLocations: ["relationship.role"],
      securityRule: "signed_in",
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
      id: "019d9b05-2502-783d-8371-5a4a2195e9f0",
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
      uiLocations: ["relationship.role"],
      securityRule: "signed_in",
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
      id: "019d9b05-2504-7a18-ae33-02fd9292fcbf",
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
      uiLocations: ["relationship.role"],
      securityRule: "signed_in",
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
      id: "019d9b05-2505-7656-8c2a-7ceb1063a2a2",
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
      uiLocations: ["relationship.role"],
      securityRule: "signed_in",
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
      id: "019d9b05-2505-7ec4-8c2b-171c82f1ea81",
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
      uiLocations: ["relationship.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-2506-7712-b1e3-52c61ba6bc08",
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
      uiLocations: ["model.relationships", "entity.relationships"],
      securityRule: "signed_in",
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
      id: "019d9b05-2508-7c72-8520-a5546333e72d",
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
      uiLocations: ["relationship"],
      securityRule: "signed_in",
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
      id: "019d9b05-2509-72c8-b2e6-d2a626b5bdc9",
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
      uiLocations: ["relationship.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-2509-792b-b2e7-388939efcf0c",
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
      uiLocations: ["relationship.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-250a-7028-be80-70c886023ac4",
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
      uiLocations: ["relationship.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-250a-7ae9-be81-54ce40d78a8b",
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
      uiLocations: ["relationship.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-250b-7293-b3c8-701812e14a1c",
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
      uiLocations: ["global"],
      securityRule: "signed_in",
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
      id: "019d9b05-250c-7aa3-a2cd-47f22c9a4ddd",
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
      uiLocations: ["model.types"],
      securityRule: "signed_in",
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
      id: "019d9b05-250d-7410-8915-5bedb78dbcbe",
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
      uiLocations: ["type"],
      securityRule: "signed_in",
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
      id: "019d9b05-250d-7b22-8916-f615fc5155cd",
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
      uiLocations: ["type.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-250e-7220-a831-669e04b51bb7",
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
      uiLocations: ["type.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-250e-7bc2-a832-baca0e8143b2",
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
      uiLocations: ["type.hidden_detail"],
      securityRule: "signed_in",
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
      id: "019d9b05-250f-7acc-a306-8b0e75fedcc1",
      groupKey: "config",
      actionKey: "ai_agents_instructions",
      title: "AI Agents Instructions",
      description:
        "Each AI Agent should read that first. Returns a usage guide for AI Agents. Use it for your AGENTS.md files if your agent doesn't support instructions in MCP.",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-2510-7160-aa20-273bffecba9e",
      groupKey: "config",
      actionKey: "inspect_config_text",
      title: "Inspect config as text file",
      description:
        "Returns a human-readable list of the configuration, including extension contributions and contribution points, what provides what to whom.",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-2510-76a3-aa21-7eb78fb715b3",
      groupKey: "config",
      actionKey: "inspect_actions",
      title: "Inspect actions",
      description:
        "Returns all known actions with their parameter descriptions.",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-2510-78fd-aa22-0b89d7253022",
      groupKey: "config",
      actionKey: "inspect_config",
      title: "Inspect config",
      description:
        "Returns a Json representation of the configuration, including extension contributions and contribution points, what provides what to whom.",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-2510-7c28-aa23-bfdd33025afc",
      groupKey: "config",
      actionKey: "inspect_permissions",
      title: "Inspect permissions",
      description:
        "Returns all known permissions registered in application with their descriptions.",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-2510-7ebc-aa24-5b1e105c656f",
      groupKey: "config",
      actionKey: "inspect_security_rules",
      title: "Inspect security rules",
      description:
        "Returns all known security rules registered in application with their descriptions.",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-2511-70f1-8392-c58ce61aa199",
      groupKey: "config",
      actionKey: "inspect_type_system",
      title: "Inspect type system",
      description:
        "Returns all known types declared in application with their description.",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "public",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-2511-79ce-8393-7601b1b9a2b7",
      groupKey: "databases",
      actionKey: "driver_list",
      title: "Database drivers",
      description: "Lists available database drivers",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
    {
      id: "019d9b05-2511-7d2b-8394-e5d668d61389",
      groupKey: "databases",
      actionKey: "datasource_list",
      title: "Database sources",
      description: "Lists available datasources",
      parameters: [],
      uiLocations: ["global"],
      securityRule: "admin",
      semantics: {
        intent: "other",
        subjects: [],
        returns: [],
      },
    },
  ],
} as const satisfies ActionRegistryDto;
