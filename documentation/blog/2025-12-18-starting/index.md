---
slug: 2025-12-18-starting-medatarun
title: Starting Medatarun
authors: [ sebastienjust ]
tags: [ announcements ]
---

Medatarun starts.

Medatarun is starting today as an open source project.

<!-- truncate -->

The code is public, the foundations are in place, and the project is released in version 0.0.0.

This is an early stage. The scope is defined, the core works, and not everything is mature yet. The intent of this first
article is simply to state what Medatarun is, what already exists, and how it can be used now.

## What Medatarun is

Medatarun is a system designed to work with multiple **shared, explicit data models** that can be explored, queried,
modified, versioned and used across different roles (business people, IT, devs, ops) and tools (CI/CD pipelines, AI,
UI).

For decades, software teams have suffered from the same underlying issue: the meaning, structure, and constraints of data exist
implicitly, scattered across code, schemas, documentation fragments, and people’s heads.

Nowadays, with the rise of AI and machine learning, with regulations becoming more and more complex, we need to be able
to work with data in a structured way that is explicit and predictable. Otherwise, AI will guess wrong, and any attempts
to build robust systems will fail. Moreover, governance is time-consuming for everybody. We need to do something about
it.

Medatarun starts from those problems, without trying to solve them through static documentation. We invite you to
read more of [our experience here](../../docs/resources/problem_en).

Yes, ambition is high because we try to address all these issues at once. Precisely, this is because those concerns are
never considered together that today's data management is unnecessarily complex.

The goal is not to produce yet another representation of data, but to make models themselves first-class, operational
objects: something that can be used by humans and automated systems together.

## One core, multiple interfaces

Medatarun is built around a single, extensible core designed as a platform, where capabilities are 
implemented as extensions rather than hard-coded features (think of "everything is a plugin", and yes, you'll be able to create your own).

Those are today's available interfaces:

| Interface                    | For                                           |                                                                                                                                                             |
|------------------------------|-----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Web interface                | Business users, IT, data analysts, developers | Intended primarily for business users (non-technical users), providing a rich, interactive UI for collaboration, exploring and manipulation of your models. |
| Model Context Protocol (MCP) | AI Agents                                     | Enables AI agents to interact directly with your models without guessing their meaning, including autonomous tagging and documentation.                     |
| REST API                     | Devs, Ops, CI/CD pipelines, external systems  | Provides structured access, imports, changes to models.                                                                                                     |
| Command Line Interface       | Developers and integrations                   | Allows direct interaction with your models in local workflows and automation contexts.                                                                      |

Note that for UI, editing capabilities exist but are still partial and evolving.

All these interfaces operate on the same underlying command system and core capabilities. There is no privileged access path and no separate source of truth.

## Current state of the project

Medatarun is usable today, with clear limitations.

- The CLI, REST API, and MCP interface are functional. They allow importing, creating, working with models, querying
  them, and exposing them to other tools or systems.
- The web UI allows exploration and visibility into the models. Some modification workflows are not yet ergonomic, even
  though all commands are technically exposed.
- Running Medatarun locally from source code is already possible and useful for certain use cases described in the
  README, such as model exploration, controlled experimentation, or integration into existing tooling.
- The server mode exists (it is the default mode) but is still young. It should not be considered production-ready as a
  SaaS platform at this stage.

## Open source scope

Medatarun is **open source software**. The code, the core system, and the interfaces are part of the same open foundation.

Professional services such as support, integration, or custom development are intentionally outside of the codebase.
They belong to the ecosystem around the project, not to the project itself.

## What to expect next

This article opens the documentation. The next steps focus on consolidating the core concepts, clarifying usage
patterns, and progressively expanding the documentation alongside the code.

No roadmap is promised here. The priority is to keep the application and its interfaces coherent and operational as the
project evolves.

If you want to go further, the rest of the documentation starts from here, and the project can already be explored and
tested locally from the source code.