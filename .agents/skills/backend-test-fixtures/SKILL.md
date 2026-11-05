---
name: backend-test-fixtures
description: Where and how to put test data in dywy's backend tests. Use when writing unit or integration tests that need domain objects, JSON documents, tokens or seed rows.
---

# Test data goes in fixtures

- Reusable test data lives in `backend/src/testFixtures/kotlin/...` as `object <Thing>Fixtures` (e.g. `InvitationFixtures`, `PostalAddressFixtures`, `GuestFixtures`, `InvitationDataFixtures`), mirroring the main package.
- Tests (`src/test`, `src/integrationTest`) must not inline JSON strings, ids, tokens or ad hoc domain objects. Add a named fixture or a parametrized builder (e.g. `rawJson(...)`) and import it.
- Fixtures must satisfy business rules: an invitation with a postal address is `POSTED`; one without is `HAND_DELIVERED` or unset. Keep `testFixtures/resources/data.sql` consistent with the Kotlin fixtures (same ids, tokens, JSON values).
- `internal` main functions are visible to testFixtures, so test helpers that wrap them (e.g. `InvitationData.rebuild()`) belong in the fixtures object too, not in each test class.
- Integration tests share one DB per class (`data.sql` runs once before the class), so each test must use guests/tokens no other test in the class uses.
- Assertions use assertk; run `./gradlew test integrationTest`.

