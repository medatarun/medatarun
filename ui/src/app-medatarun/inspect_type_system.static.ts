import type { TypeDescriptorDto } from "@medatarun/ui/business/types/TypeDescriptorDto.ts";

/** This file is auto-generated from the TypeDescriptor backend. Do not modify.*/
export const inspect_type_system_static: { items: TypeDescriptorDto[] } = {
  items: [
    {
      id: "TextMarkdown",
      equivJson: "string",
      description: "A rich formatted text.",
      enumValues: null,
    },
    {
      id: "TextSingleLine",
      equivJson: "string",
      description:
        "A text on a single line, that doesn't exceed 200 characters long.",
      enumValues: null,
    },
    {
      id: "Username",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "Fullname",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "PasswordClear",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "ActorId",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "RoleId",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "RoleKey",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "PermissionKey",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "RoleRef",
      equivJson: "string",
      description: "A reference to role.",
      enumValues: null,
    },
    {
      id: "TagId",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "TagKey",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "TagRef",
      equivJson: "string",
      description: "A reference to tag.",
      enumValues: null,
    },
    {
      id: "TagScopeRef",
      equivJson: "object",
      description: "A reference to a tag scope.",
      enumValues: null,
    },
    {
      id: "TagGroupKey",
      equivJson: "string",
      description: "",
      enumValues: null,
    },
    {
      id: "TagGroupRef",
      equivJson: "string",
      description: "A reference to tag group.",
      enumValues: null,
    },
    {
      id: "TagSearchFilters",
      equivJson: "object",
      description: "",
      enumValues: null,
    },
    {
      id: "AttributeKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
      enumValues: null,
    },
    {
      id: "BusinessKeyKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
      enumValues: null,
    },
    {
      id: "BusinessKeyRef",
      equivJson: "string",
      description: "A reference to a business key.",
      enumValues: null,
    },
    {
      id: "EntityKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
      enumValues: null,
    },
    {
      id: "EntityRef",
      equivJson: "string",
      description: "A reference to an entity attribute.",
      enumValues: null,
    },
    {
      id: "EntityAttributeRef",
      equivJson: "string",
      description: "A reference to an entity attribute.",
      enumValues: null,
    },
    {
      id: "ModelAuthority",
      equivJson: "string",
      description:
        "Canonical models are authoritative business references. System models describe imported implementations.",
      enumValues: ["system", "canonical"],
    },
    {
      id: "ModelDiffScope",
      equivJson: "string",
      description:
        "Defines how model comparison is computed: structural only or full comparison.",
      enumValues: ["STRUCTURAL", "COMPLETE"],
    },
    {
      id: "ModelKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
      enumValues: null,
    },
    {
      id: "ModelRef",
      equivJson: "string",
      description: "A reference to an entity attribute.",
      enumValues: null,
    },
    {
      id: "ModelVersion",
      equivJson: "string",
      description:
        "\nModelVersion follows Semantic Versioning (MAJOR.MINOR.PATCH).\n\nEach part is a number, for example 1.2.3. The version must not be empty.\n\nAn optional pre-release can be added after -, using dot-separated identifiers, for example 1.2.3-alpha or 1.2.3-alpha.1.\nBuild metadata after + is not accepted.\n\nNumeric identifiers (major, minor, patch, and numeric pre-release parts) must not contain leading zeros.\nPre-release identifiers may only contain letters, digits, and hyphens.\n\nThis format allows versions to be compared and ordered consistently over time.     \n",
      enumValues: null,
    },
    {
      id: "RelationshipAttributeRef",
      equivJson: "string",
      description: "A reference to a relationship attribute.",
      enumValues: null,
    },
    {
      id: "RelationshipCardinality",
      equivJson: "string",
      description: "",
      enumValues: ["zeroOrOne", "many", "one", "unknown"],
    },
    {
      id: "RelationshipKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
      enumValues: null,
    },
    {
      id: "RelationshipRef",
      equivJson: "string",
      description: "A reference to a relationship.",
      enumValues: null,
    },
    {
      id: "RelationshipRoleKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
      enumValues: null,
    },
    {
      id: "RelationshipRoleRef",
      equivJson: "string",
      description: "A reference to a relationship role.",
      enumValues: null,
    },
    {
      id: "SearchFields",
      equivJson: "array",
      description: "",
      enumValues: null,
    },
    {
      id: "SearchFilters",
      equivJson: "object",
      description: "",
      enumValues: null,
    },
    {
      id: "TypeKey",
      equivJson: "string",
      description:
        "\nA key is a technical, user-defined identifier that may originate from heterogeneous systems and becomes canonical inside Medatarun.\n\nA valid key must:\n\n- be a non-empty string with a bounded length (maximum 128 characters),\n- contain only printable ASCII characters (code points 0x20 to 0x7E),\n- not contain any ASCII control characters (0x00–0x1F and 0x7F),\n- not contain characters with implicit escaping or execution semantics: backslash (\\), single quote ('), double quote (\"), or backtick (`).\n\nNo normalization, transformation, or semantic interpretation is applied.\nThe key is compared and stored exactly as provided.\n ",
      enumValues: null,
    },
    {
      id: "TypeRef",
      equivJson: "string",
      description: "A reference to a relationship role.",
      enumValues: null,
    },
  ],
};
