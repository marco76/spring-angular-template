# Release Check

Use this checklist before a merge or deployment.

## Commands

```bash
./scripts/check.sh
./scripts/deploy-local.sh up
./scripts/smoke.sh
```

## Review

- Any database migration?
- Any API contract change?
- Any feature flag added or removed?
- Any doc update needed?
- Any new secret or environment variable?
- Any generated files that should not be committed?

## Done Means

- Checks pass.
- Local stack starts.
- Smoke checks pass.
- Release notes or PR description explain risk and verification.

