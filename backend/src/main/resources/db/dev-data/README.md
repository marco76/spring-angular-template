# Development Data

Put local Docker/PostgreSQL seed data here when an application needs sample data for manual development.

Guidelines:

- Keep schema changes in `backend/src/main/resources/db/migration`.
- Use this folder for local sample data only.
- Do not store secrets, production exports, or personal data here.
- Prefer idempotent scripts that can be safely rerun.
- Document how to load the data when a loading script exists.

