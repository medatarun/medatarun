# AGENTS.md

## Coding guidelines

- Avoid using destructuring declaration in Kotlin
- Don't create extension functions yourself
- Throw exceptions. Don't catch unless specified. All exceptions shall extend MedatarunException. Each error produces
  a distinct exception so we can catch each error in tests by Exception class name. Don't reuse other exceptions unless
  specified
- add comments on methods or code when the method or content is not obvious. 
- Comments shall not repeat code but explain what the code or method does and choices had been made and why.
- Logs are produced using slf4j
- In tests we prefer using Kotlin test assertions