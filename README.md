# medatarun

[![SafeSkill 85/100](https://img.shields.io/badge/SafeSkill-85%2F100_Passes%20with%20Notes-yellow)](https://safeskill.dev/scan/medatarun-medatarun)

**Medatarun** is an application to manage conceptual data models: 
the meaning and description of models, entities, relationships, attributes, data types, etc.

You can tag, compare, historize the models, and write your documentation. 

UI is made to be shown to business users so they can read, complete, and think 
about data. Reveal technical details when tech is your job.

API, CLI, UI, MCP (for AI Agents) share the exact same actions. It is tracked, 
versionned, auditable with precise history. You know _who_ or _what_ did what, when. 

To start, you can import the schemas you already have, or manually create new ones. 
Then express the business meaning where it belongs, transform it, add tags for 
governance needs and security, enrich types. 

This makes Medatarun a **live reference** for AI. Agents manipulating your data
can get the meaning _live_ from Medatarun, replacing their deductions with knowledge.

Humans and agents use the same operations, so a model is not just documented — it’s operational.

When time passes and your own apps evolve, re-import your database schema and
compare: actual versus wanted, staging versus production, decided versus next
release. You get the elements to act yourself, complete or discuss with your teams,
automate reporting in CI/CD, post or your Slack/Teams, or ask AI to operate itself.


[🌍 Website](https://www.medatarun.com/) • 
[📘 Documentation](https://docs.medatarun.com/) • 
[📦 Download and Install](https://docs.medatarun.com/docs/installation/install-from-distribution)

**Why**

That matters because today each group works with a different slice of the domain: schemas for devs, documents for
business, controls for governance, conventions for ops, and guesses for AI. The model changes faster than these views
can stay aligned, and everyone loses time reconstruing meaning, checking impact, or asking again “what does this field
really represent?”. Medatarun makes the domain explicit, shared, and stable across all teams, so alignment doesn’t depend
on scattered artifacts or interpretation.

Moreover, in the age of AI, relying on the model to be guessed __is no longer tenable__. Agents **need** a clear, documented, and
structured domain to operate safely and accurately. A central, explicit model becomes the only solid ground you can
expect them to act on.

Read more on [The Missing Operational Data Model](https://docs.medatarun.com/docs/resources/problem_en/)

> Said differently: you can give your AI access to your PostgreSQL, but without somewhere describing whether this column is tax included or not, your AI will not guess. Give it Medatarun.

## Key features

- **Web UI for large, evolving models**.

  Browse, inspect, update models, write documentation, add tags, and manage the models.
  Business users can really understand and adjust the models directly. 
  You get comparison tools, search, and history of changes. 
  In Medatarun, the meaning and understanding of the data comes first.

- **Rich, in-model documentation** without breaking structure

  Medatarun is made to carry real explanations: add understandable names, write
  descriptions with long-form texts (Markdown) directly on models, entities,
  relationships, and attributes. Business meaning and context
  are written where they apply.

  At the same time, text does not define structure. Operations, automation, and
  model comparisons rely on explicit structural keys (like product codes in ERP).
  So you can get comparisons of structural changes (including renamed
  columns/tables on import) as well as full text comparison of your models.

- **Support for many models and many entities**

  Each model can contain many entities and relationships, making documentation
  of your applications complete.
  Medatarun allows you to manage all your models at the same place, with clear hints on what
  models are canonicals (what you want) versus observed on your systems (what you have).

- **Extended and customizable type system**

  Models contain types. Associated with entity attributes or relationships, 
  you can express data not just with String and Int, but rich ones like Email, 
  PhoneNumber, URL, RichText, etc.

  Types are first grade model elements and can be documented with their 
  values, usages, business rules, validation rules. It largely helps team understanding and AI reasoning. 

- **Tags on elements (models, entities, relationships, attributes)**

  Create and apply tags. Use them for semantics, data location (in
  which apps, which infra), regulatory obligations (GDPR, security),
  whatever you need to get organized.

  There are two kinds of tags: 

    - **local tags** belong to one model only. When you can manage the model,
      you can create local tags on it.
    - **global tags** are managed at the application level, organized by groups,
      with specific permissions, shared by all models.

  Use global tags for governance, regulations, overviews: no pollution inside. 
  Still, let your teams tag themselves in their perimeter for their needs, without waiting for someone else's approval.

- **Tag based search**.

  Gives immediate visibility on cross-cutting concerns such as personal data, sensitivity, compliance scope, data
  quality, or business rules.

- **Unified operations for UI, CLI, APIs, and AI Agents**

  The same operations are exposed everywhere: user interface, command line, REST
  APIs, and AI Agents via MCP Protocol. An important point for tools and agents:
  it is the exact same syntax, parameters, and security everywhere. Because
  humans and automation act on the exact same primitives, it ensures
  consistency, traceability, and no difference between workflows: no glue code,
  no custom API, no parsing layer...

  For you, it is easier too. Medatarun user-interface has an _action runner_
  with live action documentation, parameters, types, and a "run" button. 
  CLI also gives this documentation. 

  Builtin commands allow to view and manipulate

    - models
    - their types
    - entities (with descriptions, tags, origins, credits)
    - relationships (cardinalities, tags, attributes).
    - attributes (for entities and relationships, with tags, rich types, and descriptions)

 - **Import models**

  Quickly start by importing your existing schemas from [your existing databases (JDBC support](https://docs.medatarun.com/docs/installation/databases) or from [Frictionless TableSchema or Data Packages](./extensions/models-import-frictionlessdata/README.md).
  After import, you get the structure that the importer inferred from the source. Then, you can
  refine it: adjust types and relationships, add tags and descriptions, and reshape entities or attributes when the
  import didn’t capture enough information.

- **Action-level traceability with explicit actors (humans and non-humans)**

  Every operation is executed by an identified actor, whether it is a human user, an external identity, an automation, a CI/CD pipeline, or an AI agent.
  Authentication (local users or external OIDC providers) and JWT validation are used to establish identity, but never blur authorship.
  This makes all changes attributable and auditable: you can know who or what modified which part of a model, when, and through which operation, across all channels.

- **Extension system**.

  Lets teams add imports, connectors, validations, or commands to UI/CLI/API/MCP without forking.
  Our extensions kernel allow extensions to extend each other via a strongly typed contribution system (Java/Kotlin
  interfaces).
  Build your own ecosystem over it.

- **Version control**

  Medatarun has a built-in version management: it stores its data in its own
  database as a history of events. Releasing a model version is like creating a 
  Git tag: it puts a version number on a specific point in history.

  Additionally, you can export your models versions (or the current unreleased version).
  This format is documented (JSON Schema) and compatible with GIT repositories:
  full text, sorted, diffable, reviewable. 

- **Open source**

  behaviour is inspectable, integrations are unrestricted.

## Derivative features

Not built-in per-se but as a consequence of how Medatarun is build

- **AI-driven advanced operations without dedicated features**.

  Because the model is unified, structured, and versioned, an agent can import SQL schemas, reconcile them with
  reference models, detect inconsistencies, propose refactors, annotate risky fields, generate multilingual
  documentation, or apply compliance tags — all via generic model primitives.
  This is the capability that shifts the tool from “modeller” to “platform for model-level automation”.

- **Model imports outside built-in features**

  Besides our JDBC and Frictionless imports, you can ask agents to ingest 
  other SQL schemas files, JSON structures, CSV samples, or even Word or 
  Confluence documentation to build a coherent domain model through our generic
  primitives. This mostly removes the need for dedicated import features 
  and makes Medatarun usable even when no clean model exists yet.

## What you can do with it

Combining our built-in features and AI, here is a sample of use-cases:

- Documenting legacy databases by importing their schema, enriching it with real business meaning, implicit rules, field
  usage, and historical context, turning an opaque structure into an understandable model.
- Aligning teams around a single domain model where descriptions, tags, and constraints accumulate incrementally instead
  of being scattered across documents.
- Enabling AI to generate correct SQL, transformations, or explanations by operating on the explicit model rather than
  guessing from ambiguous table and column names.
- Reconciling multiple heterogeneous sources by importing their schemas and reconstructing a coherent target model that
  captures shared business concepts.
- Preparing refactoring or modernization by clarifying the domain first in the model, then gradually adapting the
  underlying systems.
- Understanding impact by navigating relationships, attributes, and tags to see what a change affects before making it.
- Handling compliance and governance (e.g., personal data, sensitivity, retention rules) through systematic tagging and
  tag-based search.
- Generating consistent, multilingual documentation directly from the model, avoiding outdated or duplicated
  descriptions.

