# dywy

## Project goal
An iterative wedding planning tool: guests receive a personal invitation (QR code + magic-link sign-in) to RSVP, choose a meal, and pick a song for the wedding playlist, while the couple manages everything from a Google-authenticated backoffice.
And many other features to come

## Project structure
- `backend/`: Kotlin + Spring Boot API (DDD/Clean Architecture: `api`, `application`, `domain`, `infrastructure` packages)
- `frontend/`: Vue 3 + TypeScript, two Vite apps (`frontend/public` guest-facing app, `frontend/backoffice` staff app)
- `docs/`: dedicated documentation (deployment, integrations, security models)

## Development setup
- Prerequisites: JDK 25, Node.js and pnpm, Docker
- Start local dependencies: `docker compose up -d db mailpit`
- Backend: `./gradlew bootRun` from `backend/` directory
- Frontend: `pnpm install` then `pnpm run dev:public` or `pnpm run dev:backoffice` from `frontend/` directory

## Workflow
1. Update the base branch and create a new branch for your work. The base is `main` for simple issues and bug fixes, and `release/X.Y.Z` for a sub-issue of a parent issue planned with sub-issues (see "Release branches").
2. **Branch naming**: `feat/#<issue-number><short-description>` for features, `fix/#<issue-number><short-description>` for bug fixes (use a similarly descriptive prefix, e.g. `chore/#<issue-number><short-description>`, for other changes).
3. Make your changes, following the coding conventions below.
4. Stage the affected files with `git add` after each change.
5. Do not commit or push — leave that to the maintainer.
6. If you are an automated agent, you must never run `git commit`, `git merge`, `git rebase`, or `git push` in this repository. Leave all changes as uncommitted working tree and/or staged changes for the maintainer to review.
7. **Commit message format** (when asked to propose one): `<type>(#<issue>[, #<issue>]): <gitmoji> <short description>`, followed by a blank line and one GitHub closing line per issue (`Closes #<issue>`), with no other body. Example:
   ```
   feat(#233, #234): ✨ add postal address and delivery method stored in a jsonb invitation_data column

   Closes #233
   Closes #234
   ```

## Release branches
Only for a parent issue planned with sub-issues, so that a complete feature reaches `main` (preprod, then production) in one go. Simple issues and bug fixes keep the normal flow from `main`.
- The parent issue has a GitHub milestone named `X.Y.Z`. The release branch is named after that milestone: `release/X.Y.Z` (not after the issue number).
- Sub-issue branches (`feat/#<sub-issue>-...`) branch from `origin/release/X.Y.Z` and their PRs target it, never `main`.
- The maintainer creates `release/X.Y.Z` from `main`, keeps it current by merging `main` into it, and merges it into `main` once the whole feature is complete. Then the milestone is closed, which tags `main`. Agents never create, merge, back-merge or push release branches.
- A production hotfix branches from `main`, merges to `main` first, and is back-merged by the maintainer into open release branches.
- See the `release-branch` skill.

## Tooling for automated agents
- When the IntelliJ MCP server (`http://localhost:64342`) is available, prefer its tools (file reading, search, builds, inspections, git status) over raw terminal commands.
- Edit files with the editor tools, one file and one edit at a time. Never write Python or shell scripts or heredocs to patch files.
- After an edit, check the result (`git diff`, `grep`, or the IDE inspections) before building on it.
- Terminal commands: always run them in one reused IntelliJ terminal window (never open a new terminal tab per command), and use background mode only for genuinely long-running processes (dev servers, watchers). Kill every process you started once done so no stray terminal tabs or processes are left behind.

## Craft principles
- **KISS**: prefer the simplest implementation that satisfies the scope; avoid unnecessary abstraction, indirection, or overly clever code.
- **YAGNI**: implement only what is required by the current issue scope; do not add speculative features, extension points, or premature generalization.
- **DRY**: do not duplicate logic, rules, or data between backend and frontend, or within either. Before adding code, search for an existing helper, fixture, or constant and reuse it; extract shared code once a second copy appears. When a rule must exist on both sides (e.g. validation), keep the backend as the source of truth and have the frontend mirror it without redefining it differently.
- **No comments**: do not add code comments or KDoc/JSDoc; make the code self-explanatory through naming and structure. Only add a comment when explicitly asked or when a non-obvious constraint cannot be expressed in code.

## Testing
- Backend: 
  - Unit tests: `./gradlew test`
  - Integration tests: `./gradlew integrationTest`
  - Full verification: `./gradlew check`
- Frontend: `pnpm run test` (watch mode: `pnpm run test:watch`, coverage: `pnpm run test:coverage`)
- Backend test method names always start with `should` and describe the expected behavior; never repeat the name of the tested function/class (e.g. `should add the error for blank values`, not `addIfBlank should add the error for blank values`).
- Keep tests DRY too: build test data with shared fixtures/builders (backend `testFixtures`, frontend `testFixtures`, e2e `fixtures`) instead of copying objects or setup between tests; extract repeated arrange/act steps into helpers; use parameterized tests (`it.each`, `@ParameterizedTest`) for the same assertion over several inputs.
- Test behavior once, at the right level: do not re-assert in a higher-level test what a lower-level test already covers, and do not assert that something is absent just because it is not specified.

## Sample data
- The demo dataset `backend/src/testFixtures/resources/sample/sample-data.sql` (loaded with `load-sample-data.sql`, see [local development](docs/local-development.md#sample-data)) must cover every feature, enum value and nullable field.
- Any new feature, column or enum value must extend the sample dataset in the same issue. Keep its ids UUIDv7 with a timestamp matching `creation_date`, and keep every row scoped to the `@sample.dywy.cloud` email domain so the cleanup stays safe on preprod.

## Versioning
The release tag (`X.Y.Z`) is the single source of truth: never bump a version in an issue, and never hard-code one in `backend/build.gradle.kts` or `frontend/package.json`.
- Release builds get the version through `APP_VERSION`; other builds are inferred from git tags by the reckon Gradle plugin (`backend/build.gradle.kts`, version in `libs.versions.toml`): `<next minor>-SNAPSHOT` of the latest tag.
- The backoffice reads the version from the backend (`GET /app/version`).

## Key commands
- Run backend tests: `./gradlew test` or `./gradlew check`
- Run frontend tests: `pnpm run test`
- Start local dependencies: `docker compose up -d db mailpit`

## Environment variables
Frontend apps require `.env.development` files:
- `frontend/backoffice/.env.development`:
  ```
  VITE_API_BASE_URL=http://localhost:8080
  VITE_ROUTER_BASE=/
  ```

## Important notes
- Guest magic-link flow requires specific request order in Bruno collection for CSRF protection
- Backend uses Testcontainers for integration tests
- Backend uses Exposed with Liquibase for database operations
- Frontend uses Vite with Vue 3 and TypeScript
- Backend environment variables: 
  - `APP_MAIL_PROVIDER=smtp` (for local development with Mailpit)
  - `spring.datasource.url=jdbc:postgresql://localhost:5432/dywy_db`
  - `spring.datasource.username=admin`
  - `spring.datasource.password=<REPLACE_WITH_LOCAL_PASSWORD>` (set locally via env/secret config)

## Important constraints
- Never create local commits in this repository, whether you are a human contributor or an automated agent.
- Automated agents must stop before any history-changing Git action (`git commit`, `git merge`, `git rebase`, `git push`, `git cherry-pick`, `git reset` that would discard user work) unless the user explicitly asks for that exact Git operation.
- All code changes must be left as working tree and/or staged changes for the maintainer to review and commit manually through the normal pull-request flow.
