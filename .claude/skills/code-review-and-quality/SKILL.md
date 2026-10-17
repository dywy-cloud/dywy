---
name: code-review-and-quality
description: Conducts a multi-axis review (correctness, readability, architecture, security, performance) of a dywy change. Use before the maintainer commits, when reviewing a diff written by yourself, another agent or a human, or when asked to review a branch or pull request.
---

# Code Review and Quality

Adapted from https://github.com/addyosmani/agent-skills (code-review-and-quality) to dywy's conventions (see `AGENTS.md`).

**Approval standard:** approve when the change clearly improves code health and follows project conventions, even if it is not how you would have written it. Do not rubber-stamp ("LGTM" without evidence) and do not soften real issues.

## Process
1. **Context**: read the issue (`gh issue view <n>`) and the diff (`git status --short`, `git diff`, `git diff --staged`, `git diff main...HEAD`). Know the intended behavior and the layers in scope.
2. **Tests first**: do they exist, test behavior, cover edge cases and would catch a regression? Verify by experiment when in doubt: invert one condition (drop a negation, swap `&&`/`||`), run the tests, then restore the file. A mutation that stays green is a finding; name the missing test case.
3. **Implementation**: walk each file through the five axes below.
4. **Categorize** findings, ordered by leverage (correctness and security first, nits last).
5. **Verify the verification**: run the relevant commands and report results.

## Five axes

### 1. Correctness
- Matches the issue requirements; null, empty and boundary values handled; error paths handled.
- Business rules live in the domain (`init` invariants), not in controllers or the frontend. Backend is the source of truth; the frontend mirrors validation without redefining it.
- No race conditions, state inconsistencies or off-by-one errors.
- Liquibase: one changeset per issue (see `liquibase-changeset-per-issue`); jsonb aggregates follow `jsonb-aggregate-column`.

### 2. Readability and simplicity
- **No comments / KDoc / JSDoc** unless a non-obvious constraint cannot be expressed in code. Flag added comments.
- Names are descriptive and consistent; control flow is straightforward (no nested ternaries or deep callbacks).
- **KISS / YAGNI**: no speculative features, extension points or premature abstractions. Abstractions must earn their complexity.
- No dead code (unused variables, compat shims, `// removed` leftovers).
- A new conditional bolted onto an unrelated flow, or repeated conditionals on the same shape, signal a missing model, helper or policy.

### 3. Architecture
- Respects DDD/Clean layers (`api`, `application`, `domain`, `infrastructure`); dependencies point inward; no domain logic in `api`/`infrastructure`.
- Stays within the issue's layers (see `issue-scope-layers`); out-of-scope work is parked.
- **DRY**: reuse existing helpers, fixtures and constants instead of near-duplicates; extract once a second copy appears. No duplicated rules between backend and frontend, or between the guest (`frontend/public`) and backoffice apps.
- Refactors must reduce concepts, not just relocate them. Feature-specific logic does not leak into shared modules. Type boundaries are explicit (question gratuitous `any`, `unknown`, casts, `!!`, silent fallbacks).
- A change that grows an already large file (~1000 lines) should decompose first.

### 4. Security
- Input validated at boundaries; external data (requests, Deezer API, config) treated as untrusted.
- Authentication/authorization: backoffice endpoints respect `docs/backoffice-permissions-matrix.md` and `docs/backoffice-read-only-permissions.md`; guest endpoints respect `docs/guest-magic-link-security.md`.
- No secrets, tokens or personal data in code, logs, fixtures or sample data.
- Exposed queries are parameterized; frontend output is not rendered as raw HTML (`v-html`).
- Dependencies come from trusted sources with no known CVEs.

### 5. Performance
- No N+1 queries, unbounded fetches or missing pagination on list endpoints.
- No blocking work in hot paths; no unnecessary re-renders or watchers in Vue components.

## Project-specific checks
- **Tests**: backend test names start with `should` and describe behavior without repeating the tested function/class name; test data comes from shared fixtures/builders (`backend-test-fixtures`); `it.each` / `@ParameterizedTest` for repeated assertions; no re-assertion of lower-level coverage and no assertions of absence of unspecified behavior.
- **Sample data**: new features, columns or enum values extend `backend/src/testFixtures/resources/sample/sample-data.sql` (UUIDv7 ids matching `creation_date`, `@sample.dywy.cloud` emails only).
- **Versioning**: no version bumped or hard-coded in `frontend/package.json` or `backend/build.gradle.kts`; the release tag is the source of truth (see `AGENTS.md`).
- **Git**: nothing committed, merged, rebased or pushed by an agent; changes are staged only. Commit message proposal follows `<type>(#<issue>): <gitmoji> <description>` + `Closes #<issue>` lines.
- **Change size**: ~100 lines good, ~300 acceptable for one logical change, ~1000 too large (split). Refactoring and feature work are separate changes.

## Severity labels
| Prefix | Meaning |
|---|---|
| *(none)* | Required change before merge |
| **Critical:** | Blocks merge: vulnerability, data loss, broken functionality |
| **Nit:** | Minor and optional |
| **Consider:** | Suggestion, not required |
| **FYI** | Informational |

Propose the concrete remedy for structural problems (replace conditional chain with a typed model, collapse duplicate branches, separate orchestration from business logic, reuse the canonical helper, delete a pass-through wrapper). Quantify problems when possible. Accept the author's override when they have fuller context; comment on code, not people.

## Dead code hygiene
List code made unused by the change (functions, components, constants, fixtures) and ask before deleting anything uncertain.

## Dependencies
- Prefer the existing stack over a new dependency; check size, maintenance, license and CVEs (use the CVE validation tooling).
- Read changelogs for upgrades, one dependency per change, review lockfile diffs (`pnpm-lock.yaml`, `libs.versions.toml`), never hand-edit lockfiles.

## Verification commands
- Backend: `./gradlew check` from `backend/` (or `test` and `integrationTest` separately).
- Frontend: `pnpm run test` from `frontend/`; e2e with Playwright when UI flows changed.

## Review checklist
```markdown
## Review: [change title]

### Context
- [ ] Intent and issue scope understood

### Correctness
- [ ] Matches the issue; edge and error paths handled
- [ ] Rules live in the domain; tests cover the change

### Readability
- [ ] Clear names, no comments, no dead code, KISS/YAGNI

### Architecture
- [ ] Layers and issue scope respected; DRY; no leaked feature logic

### Security
- [ ] Input validated; authz per permission docs; no secrets/PII

### Performance
- [ ] No N+1, unbounded fetches or missing pagination

### Project rules
- [ ] Test naming/fixtures, sample data, no version bump, nothing committed

### Verification
- [ ] `./gradlew check` and `pnpm run test` green

### Verdict
- [ ] Approve
- [ ] Request changes
```

## Common rationalizations
| Rationalization | Reality |
|---|---|
| "It works" | Unreadable or misplaced code creates compounding debt. |
| "Tests pass" | Tests do not catch architecture, security or readability problems. |
| "I'll clean it up later" | Later never comes; require cleanup before merge. |
| "AI-generated code is probably fine" | It is plausible and confident even when wrong; scrutinize more. |
| "Small addition to a big file" | Judge the resulting structure, not the diff size. |
| "Just a version bump" | A bump is a behavior change you did not write; read the changelog. |

## Red flags
- Review with no severity labels, or only checking that tests pass.
- Added code comments, speculative abstractions, or duplicated helpers/fixtures.
- Domain rules implemented only in the API or the frontend.
- Out-of-scope layers touched, missing sample data or version bump.
- Security-sensitive change without a permissions/authorization check.

