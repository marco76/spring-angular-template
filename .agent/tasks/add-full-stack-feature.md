# Add Full-Stack Feature

Use this checklist for a feature that touches backend, database, API contract, and Angular UI.

## Before Coding

1. Read `docs/ARCHITECTURE.md`, `docs/CONVENTIONS.md`, and `docs/API_CONTRACT.md`.
2. Read relevant specs under `docs/specs/`.
3. Identify whether the feature changes behavior, API shape, schema, permissions, or feature flags.
4. Decide whether OpenAPI client generation exists. If it does, plan to regenerate instead of hand-writing frontend API models.

## Backend

1. Add or update Flyway migrations under `backend/src/main/resources/db/migration` when schema changes.
2. Keep local/sample data out of schema migrations unless it is required reference data.
3. Add or update request/response models, enums, validation, services, repositories, and controllers.
4. Keep transactions in services.
5. Return request/response models from controllers, not JPA entities.
6. Keep request/response models under `feature/model/` and enums under `feature/types/`.
7. Enforce protected behavior in the backend.
8. Add or update backend tests for important behavior.

## Frontend

1. Regenerate or update typed API access when available.
2. Add route-level lazy loading for new pages.
3. Prefer Angular Material controls for UI.
4. Handle loading, empty, success, and error states.
5. Keep frontend feature flags as UI visibility only.

## Documentation

1. Update `docs/API_CONTRACT.md` when endpoint meaning changes.
2. Update specs when product behavior changes.
3. Update `docs/DECISIONS.md` for durable architecture decisions.
4. Update `docs/PROJECT_STATE.md` only when future sessions need durable context.

## Verification

```bash
./scripts/check-backend.sh
./scripts/check-frontend.sh
./scripts/check.sh
```

If the local Docker stack is running:

```bash
./scripts/smoke.sh
```

For frontend production build:

```bash
cd frontend
npm run build
```

## Done Means

- Relevant specs were followed or intentionally updated.
- API contract is aligned with backend request/response models and OpenAPI.
- Database schema is managed by Flyway.
- Backend owns security and protected behavior.
- Frontend uses typed API access and Angular Material where practical.
- Checks pass, or any skipped check is explained.
