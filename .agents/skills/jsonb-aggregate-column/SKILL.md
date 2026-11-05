---
name: jsonb-aggregate-column
description: Store an aggregate (fields, nested value objects, id sets) in one jsonb column with Exposed and Jackson 3 in dywy. Use when the domain model should evolve without schema migrations.
---

# jsonb aggregate column (Exposed + Jackson 3)

Reference implementation: `infrastructure/invitation/repository/` (`InvitationData`, `InvitationTable`, `InvitationsExposedRepository`).

## Design
- A public `data class <Aggregate>Data` is the stored JSON document (not the domain entity). Naming: `<Aggregate>Data`, not `Payload`; column `<aggregate>_data`.
- `@JsonIgnoreProperties(ignoreUnknown = true)` + `schemaVersion: Int = 1`; optional fields default to `null`; required fields have no default.
- Reference other aggregates by id only (e.g. `guestIds: List<String>`, sorted). Hydrate them in one batch query (`fetchGuestsByIds`) for `findById`, `list` and lookups.
- Map data -> domain in one internal function that throws a dedicated `Inconsistent...DataException(id, reason, cause)` for missing references or domain-rule violations.
- Use Jackson 3 (`tools.jackson.module.kotlin.jacksonObjectMapper` / `readValue`; dependency `tools.jackson.module:jackson-module-kotlin`). Annotations stay in `com.fasterxml.jackson.annotation`. One shared mapper, not one per table.

## Querying inside the JSON
- No Exposed API for this: write small `Op<Boolean>` helpers next to the table (`->>`, `jsonb_exists_any(col -> 'ids', ARRAY[...]::text[])`, `NOT EXISTS (... jsonb_exists(...))`). Do not use the `?` operator (JDBC placeholder clash).
- Index lookups with an expression index, e.g. `create unique index ... on t ((col ->> 'accessToken'))`, and query with the identical `->>` expression.

## Database guarantees
- Add `check` constraints for invariants that must hold at write time (e.g. POSTED requires `postalAddress` object; `guestIds` non-empty array). Use `coalesce`/`case` so NULL does not silently pass.
- Uniqueness across array elements cannot be enforced in the DB; rely on the application check (acceptable for a single-user app).

## Tests
- Unit: JSON round trip, unknown properties ignored, defaults, missing required property fails, mapping errors raise the dedicated exception.
- Integration: raw SQL inserts of odd documents (unknown field loads; missing guest throws; check constraints reject).

