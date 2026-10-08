---
name: start-issue
description: Prepare a new dywy issue before any code change (branch from the latest main, read the issue, restore parked work, bump versions). Use whenever the user asks to start, prepare or work on the next issue.
---

# Start a dywy issue

Follow the AGENTS.md workflow literally. Run every git command with `--no-pager` (or `GIT_PAGER=cat`) so the terminal never blocks on a pager.

## Workflow
1. `git --no-pager status --short`: stop and report if the working tree already holds unrelated changes.
2. Update main: `git --no-pager fetch origin main:main` (fast-forward only).
3. Branch from `origin/main`, never from another feature branch:
   `git --no-pager switch -c <type>/#<issue>-<short-description> origin/main && git --no-pager branch --unset-upstream`
   - If the issue depends on an earlier issue, check it is already in `origin/main` (`git --no-pager log --oneline origin/main -10`). Squash merges rewrite commits, so an old feature branch is not a valid base.
   - Branch names follow AGENTS.md: `feat/#<issue>-...`, `fix/#<issue>-...`, `chore/#<issue>-...`.
4. Read the issue. `gh` may be missing: use `https://api.github.com/repos/dywy-cloud/dywy/issues/<issue>`.
5. Look for parked work from an earlier session: `git --no-pager stash list`, then `git --no-pager log -g --oneline stash` and `git --no-pager reflog` filtered on `#<issue>` if the list is empty. Apply with `git --no-pager stash pop` on the new branch (conflicts mean the stash was based on an older base: apply only the relevant files).
6. Bump versions once per issue: `pnpm run version:issue` in `frontend/`, and set the same `version` in `backend/build.gradle.kts`.
7. Stage with `git add` after each change.

## Hard rules
- Never run `git commit`, `git merge`, `git rebase`, `git push`, `git cherry-pick`, or a `git reset` that discards work, unless the user asks for that exact operation.
- Keep to the issue's layers (see `issue-scope-layers`).
- Run tools that edit the same file one at a time, never in parallel.

