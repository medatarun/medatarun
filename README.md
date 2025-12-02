# medatarun
Universal executable model engine: define, run, and evolve living domain models for humans and AI. Storage-agnostic and model-driven, it auto-exposes REST, GraphQL, and MCP interfaces through an extensible model-execution runtime.

## Key features

- **Web UI for large, evolving models**.
  
  Enables browsing, inspecting, updating and annotating complex structures at scale. 
  Business users can understand and adjust the model directly, which turns it into a shared workspace 
  instead of an engineering artefact.
  
- **MCP-based AI interaction** with full access to the model.
  
  Agents use the same commands and structures as humans, all of them. No glue code, no custom API surface, no bespoke parsing layer.
  The AI operates natively on the model.
  
- **Support for many models and many entities**
  
  tailored for real business domains and bounded contexts. 
  
- **Extended and customizable type system** (Email, PhoneNumber, URL, RichText…)
  
  Adds explicit semantics that improve validation, cross-team understanding and AI reasoning.
  
- **Free tagging on every element (models, entities, relationships, attributes)**
  
  Lets teams express semantics, organisational constraints and regulatory obligations directly 
  inside the model instead of in scattered documents.

  Tagging is free, choose your own vocabulary to adjust to your governance needs.
  
- **Tag based search**.
  
  Gives immediate visibility on cross-cutting concerns such as personal data, sensitivity, compliance scope, data quality or business rules.
  
- **Unified UI/CLI/REST/AI Agents via MCP operations**
  
  The same resources, commands and behaviours everywhere. Eliminates divergence between human workflows, automation and agents.
  Fast learning curve.
  
  Builtin commands allow to view and manipulate
  
  - models
  - their types
  - entities (with descriptions, tags, origins, credits)
  - relationships (cardinalities, tags, attributes). 
  - attributes (for entities and relationships, with tags, rich types and descriptions)
  
  Built-in plugins allow model raw imports so you can get a quick start on your assets: [Frictionless TableSchema or Data Packages](./extensions/frictionlessdata/README.md) today, Croissant in development, [JDBC support](./extensions/db/README.md) in development.
  After import, you obtain the structure that the importer could infer from the source. Medatarun operations let you refine it: adjust types and relationships, add tags and descriptions, and reshape entities or attributes when the import didn’t capture enough information. 

  

- **Multilingual and rich descriptions with stable IDs**.

  Textual content translations (names, descriptions) are stored in-place everywhere they are.
  Long texts (entity or relationship descriptions for example) accept Markdown formatting. 
  One canonical structure, multiple languages for different audiences and agents, without duplicating or drifting.

- **Extension system**. 
  
  Lets teams add imports, connectors, validations or commands to UI/CLI/API/MCP without forking. 
  Our extensions kernel allow extensions to extend each other via a strongly typed contribution system (Java/Kotlin interfaces). 
  Build your own ecosystem over it.
  
- **Version control**

  Data stored by Medatarun on your project directories are meant to be version-controlled, in plain text and human-readable formats.
  Over a GIT repository, every change—human or AI—is diffable, reviewable, reversible, and auditable 
  in the same workflow.

- **Open source**
  
   behaviour is inspectable, integrations are unrestricted.


## Derivative features

Not built-in per-se but as a consequence of how Medatarun is build

- **AI-driven advanced operations without dedicated features**.

  Because the model is unified, structured and versioned, an agent can import SQL schemas, reconcile them with
  reference models, detect inconsistencies, propose refactors, annotate risky fields, generate multilingual
  documentation, or apply compliance tags — all via generic model primitives.
  This is the capability that shifts the tool from “modeller” to “platform for model-level automation”.

- **Model import and reconstruction from existing assets**

  Agents or extensions can ingest [SQL schemas](./extensions/db/README.md), JSON structures, CSV samples or documentation and rebuild a coherent domain model through generic primitives. This removes the need for dedicated import features and makes the tool usable even when no clean model exists yet.

## What you can do with it

Combining our built-in features and AI, here is a sample of use-cases:

- Documenting legacy databases by importing their schema, enriching it with real business meaning, implicit rules, field usage and historical context, turning an opaque structure into an understandable model.
- Aligning teams around a single domain model where descriptions, tags and constraints accumulate incrementally instead of being scattered across documents.
- Enabling AI to generate correct SQL, transformations or explanations by operating on the explicit model rather than guessing from ambiguous table and column names.
- Reconciling multiple heterogeneous sources by importing their schemas and reconstructing a coherent target model that captures shared business concepts.
- Preparing refactoring or modernization by clarifying the domain first in the model, then gradually adapting the underlying systems.
- Understanding impact by navigating relationships, attributes and tags to see what a change affects before making it.
- Handling compliance and governance (e.g., personal data, sensitivity, retention rules) through systematic tagging and tag-based search.
- Generating consistent, multilingual documentation directly from the model, avoiding outdated or duplicated descriptions.

## Installation and usage

- [Installation](./docs/install.md)
- [Quickstart](./docs/quickstart.md)
- [Your project directory and data](./docs/project_data.md)
- [Build and run project from source](./docs/build_run.md) (for developers)

