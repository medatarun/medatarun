# AGENTS.md

## Coding guidelines

- Avoid using destructuring declaration in Kotlin
- Don't create extension functions yourself
- For Gradle, always use `./gradlew` from the repository root
- Throw exceptions. Don't catch unless specified. All exceptions shall extend MedatarunException. Each error produces
  a distinct exception so we can catch each error in tests by Exception class name. Don't reuse other exceptions unless
  specified
- add comments on methods or code when the method or content is not obvious. 
- Comments shall not repeat code but explain what the code or method does and choices had been made and why.
- When an interface or method carries a non-obvious contract, document that contract in code comments near the interface or method. Do not leave important local behavior described only in README files.
- Logs are produced using slf4j
- In tests we prefer using Kotlin test assertions
- Do not add default parameter values in production code. Keep call contracts explicit and handle "missing value means ..." in the implementation, not in the signature. Ask for authorization before adding a default parameter value outside test helpers.
- Avoid using the word "invariant" in explanations/comments/docs for this project; describe the concrete rule/assumption instead.
