# Project State

Use this file as lightweight memory for future agent sessions.

## Current State

- Spring Boot backend scaffold exists.
- Angular frontend scaffold exists.
- Docker local stack exists.
- MCP setup docs exist.
- Verification scripts exist.
- Angular ESLint and Prettier are configured.
- Project generator excludes build artifacts and rewrites Maven groupId from the Java package.
- Specs, dev/test database data folders, PR checklist, and full-stack feature checklist are scaffolded for AI-assisted development.
- User accounts are DB-backed (`app_user`) with an admin CRUD API and UI (`/admin/users`); see `docs/API_CONTRACT.md` and `docs/DECISIONS.md`.
- Runtime theme selection is DB-backed (`app_setting` key `ui.theme`) and switchable from the Admin page.

## Active Focus

No active project-specific feature yet.

## Notes For Future Agents

- Keep this file short.
- Update it only when there is meaningful project state that another session should inherit.
- Do not use this file for long task logs.
