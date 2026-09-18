# Next Steps

Good follow-up improvements:

1. Replace the scaffold-level Basic Auth header form with a production auth flow and protected Angular route.
2. Add one PostgreSQL Testcontainers integration test.
3. Add Playwright smoke tests.
4. Add a small app status page that shows backend health.
5. Add a feature flag system (OpenFeature or otherwise) when a project needs runtime flags.
6. Wire a CI drift check that regenerates the API client against a running backend and fails if it differs from the committed one.
