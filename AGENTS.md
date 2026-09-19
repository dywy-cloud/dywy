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
1. Update `main` and create a new branch for your work.
2. **Branch naming**: `feat/#<issue-number><short-description>` for features, `fix/#<issue-number><short-description>` for bug fixes (use a similarly descriptive prefix, e.g. `chore/#<issue-number><short-description>`, for other changes).
3. Make your changes, following the coding conventions below.
4. Stage the affected files with `git add` after each change.
5. Do not commit or push — leave that to the maintainer.
6. If you are an automated agent, you must never run `git commit`, `git merge`, `git rebase`, or `git push` in this repository. Leave all changes as uncommitted working tree and/or staged changes for the maintainer to review.

## Craft principles
- **KISS**: prefer the simplest implementation that satisfies the scope; avoid unnecessary abstraction, indirection, or overly clever code.
- **YAGNI**: implement only what is required by the current issue scope; do not add speculative features, extension points, or premature generalization.

## Testing
- Backend: 
  - Unit tests: `./gradlew test`
  - Integration tests: `./gradlew integrationTest`
  - Full verification: `./gradlew check`
- Frontend: `pnpm run test` (watch mode: `pnpm run test:watch`, coverage: `pnpm run test:coverage`)

## Versioning
For every issue, bump and keep aligned both application versions:
- `frontend/package.json` (run `pnpm run version:issue`)
- `backend/build.gradle.kts` (`version = "..."`)

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
