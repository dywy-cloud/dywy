---
name: release-branch
description: Work on or prepare a dywy release branch (release/X.Y.Z) for a parent issue planned with sub-issues. Use when an issue has a parent with a milestone, when asked to prepare a feature release, or to check a release branch is ready to merge into main.
---

# Release branch for a complete feature

Only for a parent issue planned with sub-issues. Simple issues and bug fixes use `main` (see `start-issue`). Run every git command with `--no-pager`.

## Rules
- The parent issue has a GitHub milestone `X.Y.Z`; the branch is `release/X.Y.Z`, named after the milestone, not the issue number.
- Sub-issue branches start from `origin/release/X.Y.Z` and their PRs target it.
- The maintainer creates the branch from `main`, merges `main` into it (including hotfix back-merges) and merges it into `main`. Agents never run `git merge`, `rebase`, `push` or create the release branch; they tell the maintainer what to run.
- Never bump a version: the tag created by closing the milestone is the source of truth.
- Only `main` deploys to preprod; only a tag deploys to production.

## Agent tasks
1. Sub-issue work: follow `start-issue` and `issue-scope-layers`.
2. Readiness review (on request), report each point:
   - every sub-issue of the parent is closed and its PR merged into `release/X.Y.Z`;
   - `git --no-pager log --oneline origin/release/X.Y.Z..origin/main` lists nothing the branch lacks (otherwise ask the maintainer to merge `main` in);
   - `./gradlew check`, `pnpm run test` and the e2e tests pass;
   - the sample dataset covers the new feature, columns and enum values;
   - Liquibase: no edited released changeset, no number collision, backward-compatible;
   - no hard-coded version.
3. Tell the maintainer the order: merge `release/X.Y.Z` into `main`, then close milestone `X.Y.Z` (this tags the head of `main` and triggers the release workflow).

