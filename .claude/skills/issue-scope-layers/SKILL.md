---
name: issue-scope-layers
description: Keep a dywy issue limited to its layers (domain, infrastructure, api) and park out-of-scope work. Use when a change starts touching api/application code that belongs to a later issue.
---

# Respect issue scope per layer

dywy is split by issue along DDD layers (e.g. #233 domain + persistence, #235 API/application). Do not implement a later issue's layer early.

## Workflow
1. Before finishing, run `git status --short` and check for files outside the issue's layers (`api/`, `application/`, matching tests). Do not assume they are untouched; verify.
2. Park them with a real stash, never `git restore` (which discards the work):
   `git stash push -m "#<issue> <what>" -- backend/src/main/kotlin/cloud/dywy/api backend/src/main/kotlin/cloud/dywy/application backend/src/test/kotlin/cloud/dywy/api`
3. Re-run `./gradlew test integrationTest`; new domain fields need defaults so untouched layers still compile.
4. Tell the maintainer the stash name; it is restored with `git stash pop` when the later issue starts.
5. Repo rule: never `git commit/merge/rebase/push`; staging with `git add` is fine.

## Related conventions
- KISS/YAGNI: only what the current issue needs.
- Business rules belong in the domain (`init` invariants, e.g. POSTED requires a postal address) and are mirrored by fixtures and, where useful, DB constraints.

