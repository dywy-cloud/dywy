---
name: plan-sub-issues
description: Draft the sub-issues of a dywy parent issue and the HTML mockups of the frontend screens they create or change. Use when asked to split an issue into sub-issues or to show the expected result of a UI change.
---

# Plan sub-issues

Everything lives in `docs/issues/<parent>/`, which is gitignored and never committed.

## Sub-issue drafts
- `00-parent.md` (`kind: parent`) and one `NN-<slug>.md` per sub-issue, with `title`, `layers`, `depends_on` front matter and the issue body.
- One draft = one PR, limited to its layers (see `issue-scope-layers`). Tests, docs and e2e always go in the sub-issue they belong to, never in a separate docs/e2e sub-issue.
- Agents never create or edit GitHub issues: the maintainer does it from the drafts.

## Screen mockups
- Static HTML in `docs/issues/<parent>/mockups/`: a shared `mockup.css` (dywy palette from `frontend/style.css`), one `NN-<screen>.html` per screen or state, and an `index.html` listing them. No JavaScript, no build.
- Show the expected result, not the issue text: copy the structure of the real Vue template (`frontend/public/src`) and mark each page with a `NEW` or `UPDATED` tag and the Vue file concerned; outline the changed elements with the `changed` class and a `data-change` label.
- Cover every new state (empty, selected, confirmation, error) a sub-issue introduces.
- After changing a design, update the mockup first and let the maintainer review it (open `mockups/index.html`) before coding.

## Then
The maintainer sets the milestone `X.Y.Z` and creates `release/X.Y.Z` (see `release-branch`); continue with `start-issue` on the first sub-issue.
