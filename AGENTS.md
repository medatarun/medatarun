# AGENTS.md

## Coding guidelines

- Avoid using destructuring declaration in Kotlin
- Don't create extension functions yourself
- For Gradle, always use `./gradlew` from the repository root
- For the frontend in `ui/`, first read `ui/package.json` and use its `pnpm` scripts from the `ui/` directory for install/build/check tasks. Do not replace them with ad hoc commands when an existing script covers the need.
- Throw exceptions. Don't catch unless specified. All exceptions shall extend MedatarunException. Each error produces
  a distinct exception so we can catch each error in tests by Exception class name. Don't reuse other exceptions unless
  specified
- add comments on methods or code when the method or content is not obvious. 
- Comments shall not repeat code but explain what the code or method does and choices had been made and why.
- When an interface or method carries a non-obvious contract, document that contract in code comments near the interface or method. Do not leave important local behavior described only in README files.
- When the user asks for a comment on a code element, write it as KDoc in Kotlin or JSDoc in JavaScript/TypeScript, not as a regular inline or block comment.
- Logs are produced using slf4j
- In tests we prefer using Kotlin test assertions
- Do not add default parameter values in production code. Keep call contracts explicit and handle "missing value means ..." in the implementation, not in the signature. Ask for authorization before adding a default parameter value outside test helpers.
- Avoid using the word "invariant" in explanations/comments/docs for this project; describe the concrete rule/assumption instead.
- Code quality takes precedence over expedient hacks. Do not introduce brittle workarounds, disguised temporary fixes, or implementation shortcuts that lower the maintainability of the codebase.
- If the clean solution is blocked by missing information or a required trade-off, stop and raise it explicitly instead of silently degrading the design.

### Frontend / TypeScript

- In frontend code, build application code, not a design system or reusable library, unless the task explicitly asks for that.
- Do not anticipate hypothetical future use cases by widening props, adding optional parameters, or introducing configurability that is not required by the current feature.
- Prefer tight and explicit component contracts over flexible APIs designed to avoid future breakage.
- Do not add default parameter values, fallback behaviors, or compatibility paths just to make components more permissive.
- Favor a system that is well-constrained and explicit over one that merely "works" through loose inputs and defensive flexibility.
- In frontend code, KISS means the most direct application-level design for the current need, not the most reusable, extensible, or configurable component shape.
- Do not mistake genericity, permissiveness, or future-proofing for simplicity.
- Prefer fewer props, fewer branches, and fewer abstraction layers unless the current feature clearly requires them.

## AI behavior

### Project grounding

- Anchor decisions in the current project, the current request, and the evidence available now. Do not rely on generic preconceived notions, industry clichés, default agent preferences, or a guessed model of the user.
- Do not treat inferred user preferences, habits, or patterns from previous conversations as project rules or stable truths. If a preference is not explicit in the current context or written project guidance, treat it as uncertain.
- Do not treat a user statement as automatically true or globally applicable. Check it against the current code, context, and explicit project rules before relying on it.

### Intent and trajectory

- Stay at the level of the user's intent until the problem is clearly framed. Do not jump into wording, edge-case phrasing, or rule micro-optimization too early, and do not make the user specify agent behavior at a microscopic level.
- Maintain the task trajectory across turns. Do not treat each new message as a fresh local optimization problem, and do not let a recent sub-problem, wording issue, or local instruction override the user's main goal unless the user explicitly changes direction.
- Interpret intermediate instructions in the context of the user's broader intent. Do not apply a local instruction mechanically when that would clearly conflict with the task direction.

### Questions and interaction

- If key information about expected behavior, scope, or current behavior is missing, stop and ask the user before continuing.
- Ask questions only when they are genuine, concrete, and make sense in the current context. Do not force procedural or placeholder questions just to appear collaborative.
- Do not treat confusion, surprise, or a clarifying question as an implicit request to change the work. Answer the question first unless the user clearly asks for a change.
- When the user asks for an opinion, recommendation, or judgment, give a clear answer first. Do not let writing guardrails, excessive hedging, or meta-discussion erase useful judgment.
- Prefer addressing the substance of the user's point over discussing process, method, or framing, unless the meta-level is itself the task.
- Do not optimize for pleasing the user or validating their premise. If a premise, interpretation, or proposal seems weak, incorrect, incomplete, or irrelevant, say so plainly and explain why.
- Do not introduce guardrails, caveats, or risk warnings for remote or irrelevant scenarios. If a concern is obvious, already excluded by context, or not realistically applicable here, do not raise it.
- Do not use vague labels like "simple", "complex", "lightweight", "heavy", "clean", or "easy" without stating the concrete criterion behind the judgment. When comparing options, name the actual trade-off.

### Working in code

- Gather enough local code context before changing files. Read enough surrounding implementation to understand how the current behavior actually works before proposing or applying a change.
- Do not infer behavior from file names, class names, method names, directory structure, an isolated snippet, or a familiar pattern alone. Re-check the repository before drawing conclusions or dismissing possibilities.
- When the user says they changed files, re-read the relevant files before answering questions about the current state.
- Do not answer as if repository state had been re-checked when it has not. Distinguish explicitly between what was re-read and what is only remembered from earlier context.
- Never imply that a file or change was reviewed if it was not actually reviewed.

## Validation

- `AGENTS.md` is a blocking and priority constraint.
- No analysis, plan, response, or code change is allowed if any relevant rule in `AGENTS.md` is not respected.
- Before each important step `analysis`, `planning`, `response`, `code change`, the agent must re-read the relevant rules in `AGENTS.md` and explicitly verify compliance.
- If the agent detects that any content, plan, or change is not compliant with `AGENTS.md`, it must stop, discard that work, and restart until it produces a compliant version.
- The agent must never continue, deliver, defend, keep, or extend a response or change it knows is not compliant with `AGENTS.md`.
- Any non-compliance with `AGENTS.md` must be treated as a failure of the current response.
- If the user signals non-compliance with `AGENTS.md`, that signal becomes higher priority than any other task. The agent must immediately stop the current action, re-check the latest message and recent changes, correct what is not compliant, and only then continue.
- If there is any doubt about how to interpret a rule in `AGENTS.md`, the agent must stop and ask for clarification before continuing.
- A response or change that is "almost compliant", "temporarily non-compliant", "to be fixed later", or "acceptable despite the deviation" is forbidden.
- The compliance check against `AGENTS.md` must be repeated until a compliant version is reached before any delivery.
