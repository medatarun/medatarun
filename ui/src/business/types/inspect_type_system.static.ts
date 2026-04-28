import type { TypeDescriptorDto } from "@/business/types/TypeDescriptorDto.ts";

/**
 * To regenerate the list, run the action "config/inspect_type_system" and copy
 * the output here
 */
export const inspect_type_system_static: { items: TypeDescriptorDto[] } = {
  items: [
    {
      id: "Username",
      equivJson: "string",
      description: "",
    },
    {
      id: "Fullname",
      equivJson: "string",
      description: "",
    },
    {
      id: "PasswordClear",
      equivJson: "string",
      description: "",
    },
    {
      id: "ActorId",
      equivJson: "string",
      description: "",
    },
    {
      id: "RoleId",
      equivJson: "string",
      description: "",
    },
    {
      id: "RoleKey",
      equivJson: "string",
      description: "",
    },
    {
      id: "PermissionKey",
      equivJson: "string",
      description: "",
    },
    {
      id: "RoleRef",
      equivJson: "string",
      description: "A reference to role.",
    },
    {
      id: "TagId",
      equivJson: "string",
      description: "",
    },
    {
      id: "TagKey",
      equivJson: "string",
      description: "",
    },
    {
      id: "TagRef",
      equivJson: "string",
      description: "A reference to tag.",
    },
    {
      id: "TagScopeRef",
      equivJson: "object",
      description: "A reference to a tag scope.",
    },
    {
      id: "TagGroupKey",
      equivJson: "string",
      description: "",
    },
    {
      id: "TagGroupRef",
      equivJson: "string",
      description: "A reference to tag group.",
    },
    {
      id: "TagSearchFilters",
      equivJson: "object",
      description: "",
    },
    {
      id: "AttributeKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
    },
    {
      id: "BusinessKeyKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
    },
    {
      id: "BusinessKeyRef",
      equivJson: "string",
      description: "A reference to a business key.",
    },
    {
      id: "EntityKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
    },
    {
      id: "EntityRef",
      equivJson: "string",
      description: "A reference to an entity attribute.",
    },
    {
      id: "EntityAttributeRef",
      equivJson: "string",
      description: "A reference to an entity attribute.",
    },
    {
      id: "TextMarkdown",
      equivJson: "string",
      description: "A rich formatted text in Markdown format.",
    },
    {
      id: "TextSingleLine",
      equivJson: "string",
      description:
        "A text on a single line, that doesn't exceed 200 characters long.",
    },
    {
      id: "ModelAuthority",
      equivJson: "string",
      description:
        "Canonical models are authoritative business references. System models describe imported implementations.",
    },
    {
      id: "ModelDiffScope",
      equivJson: "string",
      description:
        "Defines how model comparison is computed: structural only or full comparison.",
    },
    {
      id: "ModelKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
    },
    {
      id: "ModelRef",
      equivJson: "string",
      description: "A reference to an entity attribute.",
    },
    {
      id: "ModelVersion",
      equivJson: "string",
      description:
        "\nModelVersion follows Semantic Versioning (MAJOR.MINOR.PATCH).\n\nEach part is a number, for example 1.2.3. The version must not be empty.\n\nAn optional pre-release can be added after -, using dot-separated identifiers, for example 1.2.3-alpha or 1.2.3-alpha.1.\nBuild metadata after + is not accepted.\n\nNumeric identifiers (major, minor, patch, and numeric pre-release parts) must not contain leading zeros.\nPre-release identifiers may only contain letters, digits, and hyphens.\n\nThis format allows versions to be compared and ordered consistently over time.     \n",
    },
    {
      id: "RelationshipAttributeRef",
      equivJson: "string",
      description: "A reference to a relationship attribute.",
    },
    {
      id: "RelationshipCardinality",
      equivJson: "string",
      description: "",
    },
    {
      id: "RelationshipKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
    },
    {
      id: "RelationshipRef",
      equivJson: "string",
      description: "A reference to a relationship.",
    },
    {
      id: "RelationshipRoleKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
    },
    {
      id: "RelationshipRoleRef",
      equivJson: "string",
      description: "A reference to a relationship role.",
    },
    {
      id: "SearchFields",
      equivJson: "array",
      description: "",
    },
    {
      id: "SearchFilters",
      equivJson: "object",
      description: "",
    },
    {
      id: "TypeKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
    },
    {
      id: "TypeRef",
      equivJson: "string",
      description: "A reference to a relationship role.",
    },
  ],
};
