---
name: liquibase-changeset-per-issue
description: Write or revise a Liquibase migration in dywy's backend. Use whenever a schema change is needed for an issue, or when migrations for the same issue pile up.
---

# Liquibase: one changeset per issue

Migrations live in `backend/src/main/resources/db/changelog/` and are included from `db.changelog-master.yaml`.

## Rules
- **One changeset (one file) per issue.** If the schema evolves while working on the same issue, merge everything into that single file instead of adding `014`, `015`, ... This applies only to changesets not yet in a release tag.
- **Never edit, rename or reorder a changeset that is in a release tag**: production has already applied it and the checksum would fail. Fix it with a new changeset.
- Migrations must be backward-compatible with production data (expand/contract: add nullable or defaulted, backfill, constrain later; drop only once no released code reads it).
- On a `release/X.Y.Z` branch, check the changelog for number collisions with `main` (hotfixes) before finishing.
- Name the file after the whole change (`013-add-invitation-data-jsonb.sql`), not just its first step. Rename it and update the master include if the scope grows.
- Never add a column/table only to drop it later in the same changeset. Write the final state directly.
- Backfill existing rows in the same changeset (e.g. `jsonb_build_object` / `jsonb_agg`), then add `not null`, constraints, indexes, and drop obsolete columns/tables.
- Always provide a complete `--rollback` (restore dropped columns/tables/data, drop new constraints/indexes, in order).
- Before finishing: no leftover files on disk, master only references existing files, `git status` shows no stale `014`/`015`.

## Checklist
1. `--liquibase formatted sql` + `--changeset elgregos:<n>`.
2. Update test seed `backend/src/testFixtures/resources/data.sql` (truncate list, inserts).
3. Extend the sample dataset `backend/src/testFixtures/resources/sample/sample-data.sql` with the new column, table or enum value (every state must appear at least once), and check it still loads on the migrated schema.
4. Run `./gradlew test integrationTest` (Testcontainers applies the migration).

