---
name: start-issue
description: Prepare a new dywy issue before any code change (read the issue, branch from the latest main or its release/X.Y.Z branch, restore parked work). Use whenever the user asks to start, prepare or work on the next issue.
---

# Start a dywy issue

Follow the AGENTS.md workflow literally. Run every git command with `--no-pager` (or `GIT_PAGER=cat`) so the terminal never blocks on a pager.

## Workflow
1. `git --no-pager status --short`: stop and report if the working tree already holds unrelated changes.
2. Read the issue. `gh` may be missing: use `https://api.github.com/repos/dywy-cloud/dywy/issues/<issue>`. Note whether it is a sub-issue of a parent issue that has a milestone `X.Y.Z` (the parent is in the issue's `parent_issue_url`, the milestone on the parent).
3. Pick the base:
   - Sub-issue and `git --no-pager ls-remote --heads origin release/X.Y.Z` finds the branch: base is `origin/release/X.Y.Z`. Update it with `git --no-pager fetch origin release/X.Y.Z`. If the release branch does not exist yet, stop and ask the maintainer to create it.
   - Simple issue, bug fix or production hotfix: base is `origin/main`. Update it with `git --no-pager fetch origin main:main` (fast-forward only).
4. Branch from the base, never from another feature branch:
   `git --no-pager switch -c <type>/#<issue>-<short-description> <base> && git --no-pager branch --unset-upstream`
   - If the issue depends on an earlier issue, check it is already in the base (`git --no-pager log --oneline <base> -10`). Squash merges rewrite commits, so an old feature branch is not a valid base.
   - Branch names follow AGENTS.md: `feat/#<issue>-...`, `fix/#<issue>-...`, `chore/#<issue>-...`. Create `release/X.Y.Z` only locally, from `origin/main`, and never push it.
5. Look for parked work from an earlier session: `git --no-pager stash list`, then `git --no-pager log -g --oneline stash` and `git --no-pager reflog` filtered on `#<issue>` if the list is empty. Apply with `git --no-pager stash pop` on the new branch (conflicts mean the stash was based on an older base: apply only the relevant files).
6. Do not bump any version: the release tag is the source of truth (see AGENTS.md, Versioning).
7. Stage with `git add` after each change.

## Hard rules
- Never run `git commit`, `git merge`, `git rebase`, `git push`, `git cherry-pick`, or a `git reset` that discards work, unless the user asks for that exact operation.
- Keep to the issue's layers (see `issue-scope-layers`).
- Run tools that edit the same file one at a time, never in parallel.

