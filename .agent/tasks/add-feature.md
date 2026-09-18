# Add Feature

Use this checklist when adding an end-to-end feature.

## Steps

1. Read `docs/ARCHITECTURE.md`, `docs/CONVENTIONS.md`, and `docs/API_CONTRACT.md`.
2. Read relevant specs under `docs/specs/`.
3. Decide whether the feature needs a flag. If yes, update backend enforcement and frontend visibility.
4. Add or update backend request/response models, enums, services, controllers, and tests.
5. Keep request/response models under `feature/model/` and enums under `feature/types/`.
6. Add a Flyway migration if schema changes are needed.
7. Update or generate frontend API types.
8. Add the Angular route/page/component/service.
9. Prefer Angular Material controls for UI.
10. Handle loading, empty, and error states.
11. Run relevant checks.
12. Update docs if the architecture, API, specs, or workflow changed.

## Done Means

- Backend behavior is tested or explicitly low-risk.
- Frontend builds.
- API contract is aligned.
- Feature flags are documented and enforced server-side where needed.
