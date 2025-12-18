---
sidebar_position: 2
---

# The Missing Operational Data Model

:::note
    This text had been translated by AI from French [🇫🇷Le modèle de données opérationnel manquant](./problem_fr.md)
:::

For more than 30 years, I’ve been running into the same problem over and over again.

Every time I join a software project, I ask the same simple question: what data are we actually managing? I ask
developers, project managers, business people, and every time the answer is the same: nobody can really answer.

- Business teams show me screens, which are only partial, transformed interpretations of the data. 
- Developers tell me to look at database schemas and random files scattered through the codebase, buried in business logic and persistence
layers, in other words: the implementation. 
- Project managers say “with a bit of luck it’s up to date on Confluence”, which is usually a bad sign. 
- Sometimes someone pulls out a Word document written ten years ago that didn’t survive the
evolution of the software.

Nobody can give a global view: relationships, business meaning, invariants, operations. I end up doing document
archaeology, endless interviews, cross-checking information, digging through piles of code, SQL, and live databases that
are rarely accessible. And once it’s all in my head, what then? Do it all again on the next project?

## So we just need to document it? Fail.

Every attempt at documenting data has failed. It’s always heavy, slow work, disconnected from software that keeps
evolving. Nobody wants to maintain it, and little by little the documentation dies. I tried automation too, in both
directions: conceptual business models pushed toward code with layers of transformers and manual work, code
introspection with annotations, SQL exported to wikis. Nothing worked.

Why? Because in practice, documentation brings no operational value.

Another point: we’ve always compensated by telling ourselves that this knowledge lived in people’s heads. Collective
human knowledge was “good enough”.

Most of the time, it worked. Until it didn’t.

Some examples from experience: 
- the only person who understood inheritance and succession logic left, making the project
unmaintainable and impossible to update for regulatory reasons, so it had to be abandoned. 
- The people who denormalized the data were gone, nobody could explain why, and by the time we understood, it was too late: customers were blocked by
performance issues. 
- A client urgently asks for a GDPR-style data report before buying: nights spent digging everywhere. 
- Governance blocks changes in architecture review boards because there is no visibility.

There are plenty more stories like that.

## Rise of Governance

The context has changed.

Data governance is now front and center, driven by regulations piling up: GDPR, ISO 27001, and others. Today we need to
know exactly what data we have, what we process, where it lives, which regulations apply, whether it’s sensitive.
Listing and cataloging all this data is necessary, tedious, unproductive… and frankly painful.

In most companies I’ve worked with, this is handled through occasional audits that end up as massive Excel spreadsheets.
They’re never maintained, never updated. The process comes with endless meetings to analyze, collect, and understand the
data. Six months later, the work is already obsolete. Even when data management is included in governance processes,
steering committees, architecture boards, it still fails.

Once again, there is no operational reality behind it. It’s about compliance, not value. A constraint, not an asset. But
it has to be done.

## Here Comes a New Challenger

And then a new player arrived: AI, along with agents and assistants. At first it’s impressive. Give them database
schemas and some code, and they manage reasonably well. It even looks admirable with modern, well-designed databases.

Now give them your slightly legacy projects, or the ones built in a hurry. You know the ones: columns limited to six
characters, everything stored as VARCHAR, even dates. Or just CSV headers, for a laugh.

Very quickly, you hit a wall: the AI **guesses** the meaning of data, and often guesses wrong.

In the end, it’s no worse than humans. Without explanation, misunderstanding is the same, it just happens faster.

There’s no real dialogue to explain things gradually as the AI explores. It has no access to the shared “we all know”
understanding, nor to what was in the project manager’s head, or the business owner’s, or the developer’s, especially
when they’ve been gone for years.

At some point, documentation is no longer a “nice to have”. It becomes **mandatory**.

# Same Data, Different Views

So how do you manage a data documentation system that works for business teams, developers, IT, data analysts, and AI
agents at the same time?

That problem is still unsolved.

Each group has different needs and perspectives. Business users don’t operate in a way that lets them talk data or
technical concepts. Technical teams think in code, tables, and types, which means nothing to business and often pushes
them away. Governance needs to know what exists and be able to generate reports to decide. Project managers need to know
what’s up to date, and be warned when data structures evolve in chaotic ways. Architects need visibility without
spending nights chasing projects to detect new sensitive data or changes in usage.

Everyone works on the same thing, but not in the same way, and not with the same view.

That’s also why every change meets resistance and political deadlock forms so quickly.

The artifacts we’ve used so far, Excel files, Word documents, Confluence pages, database schemas, are not connected.
Business people won’t read database schemas. Excel files are obsolete the moment they’re produced and only interest
governance.

## A Static Problem

Another issue: these documentation artifacts are static. They don’t live, they don’t interact with anything. They
describe after the fact what the system already does, or worse, what people think it does. That’s why they die. It’s not
negligence or bad will, it’s simply useless in real workflows.

By contrast, database schemas, extraction code, ETLs are maintained because they are operational. If they break, the
whole system breaks.

## A Possible Path

The blind spot in all this is that we’ve never treated the conceptual data model as a living system: queryable,
modifiable, versioned, integrated into development processes, with proper user interfaces, search, reporting, and
everything needed to work with it operationally.

With AI, this becomes urgent. We need a system that can be queried through APIs or MCP, so AI can understand data: its
meaning, invariants, usage, what’s inside. That’s how we generate real help for BI users, precise user stories,
assistance for developers and agents to code and document, and allow governance to query what it owns in human terms.

Conceptual data models must become explicit, shared, versioned objects, enriched and directly usable by humans across
roles, by AI, by CI/CD automation, by the whole technical chain.

This canonical model now has intrinsic value: it is consulted to understand and enrich, used to decide, exploited to
generate, validate, analyze, and precise enough for AI to consume without guessing. It already **knows**.

Once such a model exists and is actually useful, the dynamic reverses. Maintaining it is no longer “extra”. It becomes a
condition for the system to function.

The question stops being “how do we document data?” and becomes “how do we keep a shared, operational data model alive,
for all actors in the system, human and technical alike?”

## Your Feedback

That’s where this project comes from. Not to build yet another governance tool, there are plenty of those, but because I
never found a satisfying answer to this problem.

So I started this: [Medatarun](https://github.com/medatarun/medatarun).

I’m not trying to convince anyone. What matters to me now is understanding how this works where you are. Do you face the
same problems? Does this gap in real data understanding resonate with you? Does this description match your experience,
or do you see the problem differently?

I’m mainly interested in feedback: experiences, disagreements, things I’ve missed.

And if you’re facing this kind of problem and don’t know where to start, that’s exactly how this project began. Come and
talk about it.