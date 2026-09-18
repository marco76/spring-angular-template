# Add API Endpoint

Use this checklist when adding or changing a Spring endpoint.

## Steps

1. Define request/response models under the feature's `model` package.
2. Add service behavior before controller wiring when business logic is non-trivial.
3. Keep entities out of controller responses.
4. Add validation annotations to request models.
5. Put backend enums under the feature's `types` package.
6. Add tests for service logic and important controller behavior.
7. Check whether the endpoint requires authentication or authorization.
8. Check whether a feature flag should enforce access.
9. Update OpenAPI-generated frontend client when generation is enabled.
10. Update `docs/API_CONTRACT.md` if the API meaning changed.

## Done Means

- Endpoint is documented through OpenAPI.
- Frontend contract is updated.
- Security and flag behavior are explicit.
