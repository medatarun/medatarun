# Medatarun frontend

## Dependencies

### Runtime Dependencies

| Package                          | Why this dependency is present                                                                                                                                                                  |
| -------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `@fluentui/react-components`     | Fluent UI design system: component library used to build React screens. As used by `@seij/common-ui` too                                                                                        |
| `@fluentui/react-icons`          | Fluent UI icons because those in `@seij/common-ui` are not enough.                                                                                                                              |
| `@seij/common-services`          | `@seij`: common services to talk to backend and manage connexions.                                                                                                                              |
| `@seij/common-types`             | `@seij`: common business types.                                                                                                                                                                 |
| `@seij/common-ui`                | `@seij`: provides shell and common inputs and layouts, based on FluentUI.                                                                                                                       |
| `@seij/common-ui-auth`           | `@seij`: authentication and security utils on top of `@seij/common-ui`. Manages authentication, menu security, OIDC with react-context-oidc. Provide current user informations and login flows. |
| `@seij/common-ui-csv-export`     | `@seij`: tool to generate CSV based on data tables, then download it to user, not tied to the design system.                                                                                    |
| `@seij/common-ui-icons`          | `@seij`: shared icon set used by the application.                                                                                                                                               |
| `@seij/common-validation`        | `@seij`: toolkit and design pattern for validation and input handling.                                                                                                                          |
| `@tanstack/react-query`          | Tanstack query: Server-state fetching, caching, and synchronization on the client.                                                                                                              |
| `@tanstack/react-query-devtools` | Tanstack query: debugging tools used during development.                                                                                                                                        |
| `@tanstack/react-router`         | Tanstack router: Client-side SPA routing and navigation.                                                                                                                                        |
| `@toast-ui/editor`               | ToastUI: Rich text WYSIWYG Markdown editor used for description editing.                                                                                                                        |
| `@toast-ui/react-editor`         | ToastUI: React wrapper for Toast UI Editor.                                                                                                                                                     |
| `lodash-es`                      | General utility functions used everywhere.                                                                                                                                                      |
| `mdast-util-to-string`           | Markdown: Readable text extraction from Markdown AST nodes (description previews).                                                                                                              |
| `react`                          | React: core runtime for the UI.                                                                                                                                                                 |
| `react-dom`                      | React: integration with the browser DOM.                                                                                                                                                        |
| `react-markdown`                 | Markdown: React component to render Markdown.                                                                                                                                                   |
| `remark-parse`                   | Markdown: Parses mardown into `mdast` AST for structured processing.                                                                                                                            |
| `unified`                        | Markdown: Content processing pipeline used with `remark-parse`.                                                                                                                                 |
| `uuid`                           | Unique identifier generation in the frontend.                                                                                                                                                   |

### Development Dependencies

| Package                       | Why this dependency is present                                           |
| ----------------------------- | ------------------------------------------------------------------------ |
| `@eslint/js`                  | Base ESLint rule configuration for JavaScript/TypeScript.                |
| `@types/lodash-es`            | TypeScript type definitions for `lodash-es`.                             |
| `@types/node`                 | Node.js type definitions for tooling (`vite`, config, scripts).          |
| `@types/react`                | TypeScript type definitions for React.                                   |
| `@types/react-dom`            | TypeScript type definitions for React DOM.                               |
| `@vitejs/plugin-react-swc`    | Vite React plugin using SWC for fast compilation.                        |
| `eslint`                      | Main linter used by `pnpm lint`.                                         |
| `eslint-plugin-react-hooks`   | ESLint rules specific to React Hooks usage.                              |
| `eslint-plugin-react-refresh` | ESLint rules related to correct React Fast Refresh behavior.             |
| `globals`                     | Global variable sets used in ESLint configuration.                       |
| `prettier`                    | Automated code formatting (`pnpm format`, `pnpm format:check`).          |
| `typescript`                  | TypeScript compiler and type checking (`tsc -b`).                        |
| `typescript-eslint`           | TypeScript integration for ESLint (parser + rules).                      |
| `vite`                        | Bundler/dev server used by `pnpm dev`, `pnpm build`, and `pnpm preview`. |
| `vitest`                      | Frontend unit test runner used by `pnpm test`.                           |
