# Add Database Migration

Use this checklist when changing schema or required reference data.

## Steps

1. Add a new Flyway migration under `backend/src/main/resources/db/migration`.
2. Use the next version number, for example `V2__create_customer.sql`.
3. Do not edit old migrations that may already be shared.
4. Keep SQL PostgreSQL-first.
5. Keep local sample data in `backend/src/main/resources/db/dev-data`, not schema migrations.
6. Keep automated test fixtures in `backend/src/test/resources/db/test-data`.
7. Update entities/repositories/services as needed.
8. Run backend tests.
9. Prefer a PostgreSQL/Testcontainers test for persistence behavior.

## Done Means

- Migration applies to an empty database.
- Hibernate validation passes.
- Code and schema agree.
